package com.jovanpets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jovanpets.document.Cliente;
import com.jovanpets.document.Producto;
import com.jovanpets.domain.CategoriaProducto;
import com.jovanpets.domain.ModalidadEntrega;
import com.jovanpets.domain.Rol;
import com.jovanpets.dto.LineaPedidoRequest;
import com.jovanpets.dto.PedidoRequest;
import com.jovanpets.exception.ConflictoException;
import com.jovanpets.exception.NegocioException;
import com.jovanpets.repository.PedidoRepository;
import com.jovanpets.repository.ProductoRepository;
import com.jovanpets.repository.SecuenciaRepository;
import com.jovanpets.security.UsuarioAutenticado;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias de las reglas de pedidos con dependencias simuladas. */
class PedidoServiceTest {
  private final PedidoRepository pedidos = mock(PedidoRepository.class);
  private final ProductoRepository productos = mock(ProductoRepository.class);
  private final InventarioService inventario = mock(InventarioService.class);
  private final ClienteService clientes = mock(ClienteService.class);
  private final SecuenciaRepository secuencias = mock(SecuenciaRepository.class);
  private final Clock reloj = Clock.fixed(Instant.parse("2030-01-10T15:00:00Z"), ZoneId.of("America/Bogota"));
  private final UsuarioAutenticado cliente =
      new UsuarioAutenticado("u1", "ana@correo.com", "Ana", Rol.CLIENTE, "c1");
  private PedidoService servicio;

  @BeforeEach
  void preparar() {
    servicio = new PedidoService(pedidos, productos, inventario, clientes, secuencias, reloj);
    Cliente c = new Cliente();
    c.setId("c1");
    c.setNombre("Ana");
    c.setDireccion("Calle 1");
    when(clientes.obtenerActivo("c1")).thenReturn(c);
  }

  @Test
  void calculaElTotalEnElBackendYAgrupaLineasRepetidas() {
    when(productos.findAllById(any())).thenReturn(List.of(producto("p1", "10000.50", 10, false)));
    var cotizacion =
        servicio.cotizar(List.of(new LineaPedidoRequest("p1", 2), new LineaPedidoRequest("p1", 1)));
    assertThat(cotizacion.detalles()).hasSize(1);
    assertThat(cotizacion.detalles().get(0).cantidad()).isEqualTo(3);
    assertThat(cotizacion.total()).isEqualByComparingTo("30001.50");
  }

  @Test
  void rechazaCantidadesMayoresAlStock() {
    when(productos.findAllById(any())).thenReturn(List.of(producto("p1", "5000", 2, false)));
    assertThatThrownBy(() -> servicio.cotizar(List.of(new LineaPedidoRequest("p1", 3))))
        .isInstanceOf(ConflictoException.class)
        .hasMessageContaining("Stock insuficiente");
  }

  @Test
  void noVendeEnLineaProductosQueRequierenFormula() {
    when(productos.findAllById(any())).thenReturn(List.of(producto("p1", "5000", 10, true)));
    assertThatThrownBy(() -> servicio.cotizar(List.of(new LineaPedidoRequest("p1", 1))))
        .isInstanceOf(NegocioException.class)
        .hasMessageContaining("formula");
  }

  @Test
  void compensaLasReservasSiUnProductoNoAlcanza() {
    Producto p1 = producto("p1", "1000", 10, false);
    Producto p2 = producto("p2", "2000", 1, false);
    when(productos.findAllById(any())).thenReturn(List.of(p1, p2));
    when(inventario.reservar(eq("p1"), anyInt(), anyString())).thenReturn(p1);
    when(inventario.reservar(eq("p2"), anyInt(), anyString())).thenThrow(new ConflictoException("Stock insuficiente"));

    PedidoRequest r =
        new PedidoRequest(
            List.of(new LineaPedidoRequest("p1", 2), new LineaPedidoRequest("p2", 5)),
            ModalidadEntrega.RECOGIDA, null, null);

    assertThatThrownBy(() -> servicio.crear(r, cliente)).isInstanceOf(ConflictoException.class);
    verify(inventario).liberar("p1", 2);
    verify(pedidos, never()).save(any());
  }

  @Test
  void elPedidoADomicilioUsaLaDireccionDelClienteSiNoSeIndica() {
    Producto p1 = producto("p1", "1000", 10, false);
    when(productos.findAllById(any())).thenReturn(List.of(p1));
    when(inventario.reservar(eq("p1"), anyInt(), anyString())).thenReturn(p1);
    when(secuencias.siguiente("pedidos")).thenReturn(7L);
    when(pedidos.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var respuesta =
        servicio.crear(
            new PedidoRequest(List.of(new LineaPedidoRequest("p1", 4)), ModalidadEntrega.DOMICILIO, " ", null),
            cliente);

    assertThat(respuesta.numero()).isEqualTo("JP-2030-000007");
    assertThat(respuesta.direccionEntrega()).isEqualTo("Calle 1");
    assertThat(respuesta.total()).isEqualByComparingTo("4000");
  }

  private static Producto producto(String id, String precio, int stock, boolean formula) {
    Producto p = new Producto();
    p.setId(id);
    p.setNombre("Producto " + id);
    p.setCategoria(CategoriaProducto.ACCESORIOS);
    p.setPrecio(new BigDecimal(precio));
    p.setStock(stock);
    p.setRequiereFormula(formula);
    return p;
  }
}
