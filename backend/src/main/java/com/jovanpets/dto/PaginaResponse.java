package com.jovanpets.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Pagina de resultados (HU-04). */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

  public static <T> PaginaResponse<T> de(Page<T> page) {
    return new PaginaResponse<>(
        page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }
}
