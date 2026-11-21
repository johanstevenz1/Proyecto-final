package com.jovanpets.dto;

import com.jovanpets.domain.Especie;
import com.jovanpets.domain.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** HU-05: datos de la mascota. propietarioId es obligatorio para el personal; el cliente usa su ficha. */
public record MascotaRequest(
    String propietarioId,
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,
    @NotNull(message = "La especie es obligatoria") Especie especie,
    @NotBlank(message = "La raza es obligatoria") @Size(max = 60) String raza,
    @NotNull(message = "El sexo es obligatorio") Sexo sexo,
    @NotNull(message = "La fecha de nacimiento es obligatoria") @PastOrPresent(message = "La fecha de nacimiento no puede ser futura") LocalDate fechaNacimiento,
    @NotNull(message = "El peso es obligatorio") @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero") @DecimalMax(value = "200", message = "Peso fuera de rango") @Digits(integer = 3, fraction = 2) BigDecimal peso) {}
