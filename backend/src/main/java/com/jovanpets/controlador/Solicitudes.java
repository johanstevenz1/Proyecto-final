package com.jovanpets.controlador;

import com.jovanpets.dominio.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public final class Solicitudes {
  public record Registro(
      @NotBlank @Size(max = 120) String nombre,
      @NotBlank @Size(max = 30) String cedula,
      @NotBlank @Email @Size(max = 180) String email,
      @NotBlank @Size(min = 8, max = 72) String clave,
      @NotBlank @Size(max = 40) String telefono,
      @NotBlank @Size(max = 250) String direccion) {}

  public record Reserva(
      @NotNull Long mascotaId, @NotNull Long servicioId, @NotNull LocalDateTime inicio) {}

  public record CambioCita(@NotNull EstadoCita estado, @Size(max = 1000) String motivo) {}

  public record Movimiento(
      @NotNull Long productoId,
      @NotNull TipoMovimiento tipo,
      @Min(1) @Max(1000000) int cantidad,
      @NotBlank @Size(max = 500) String motivo) {}

  public record Linea(@NotNull Long productoId, @Min(1) @Max(10000) int cantidad) {}

  public record Compra(
      @NotNull ModalidadEntrega modalidad,
      @Size(max = 250) String direccionEntrega,
      @NotEmpty @Size(max = 100) List<@jakarta.validation.Valid Linea> lineas,
      Long clienteId) {}

  public record CambioPedido(@NotNull EstadoPedido estado) {}

  public record CambioRol(@NotNull Rol rol, boolean activo) {}
}
