package com.jovanpets.dto;

import java.time.Instant;

public record CambioClienteResponse(Instant fecha, String usuario, String detalle) {}
