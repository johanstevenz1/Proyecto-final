package com.jovanpets.mapper;

import com.jovanpets.document.CambioCliente;
import com.jovanpets.document.Cliente;
import com.jovanpets.dto.CambioClienteResponse;
import com.jovanpets.dto.ClienteDetalleResponse;
import com.jovanpets.dto.ClienteResponse;
import com.jovanpets.dto.MascotaResponse;
import java.util.Comparator;
import java.util.List;

public final class ClienteMapper {
  private ClienteMapper() {}

  public static ClienteResponse aResponse(Cliente c) {
    return new ClienteResponse(
        c.getId(), c.getNombre(), c.getCedula(), c.getTelefono(), c.getEmail(), c.getDireccion(), c.isActivo(),
        c.getFechaRegistro());
  }

  public static ClienteDetalleResponse aDetalle(Cliente c, List<MascotaResponse> mascotas) {
    List<CambioClienteResponse> historial =
        c.getHistorial().stream()
            .sorted(Comparator.comparing(CambioCliente::getFecha).reversed())
            .map(h -> new CambioClienteResponse(h.getFecha(), h.getUsuario(), h.getDetalle()))
            .toList();
    return new ClienteDetalleResponse(aResponse(c), historial, mascotas);
  }
}
