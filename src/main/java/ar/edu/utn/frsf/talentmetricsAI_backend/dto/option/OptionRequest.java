package ar.edu.utn.frsf.talentmetricsAI_backend.dto.option;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionRequest(
        @NotNull(message = "El orden de visualización es obligatorio.") @Min(value = 1, message = "El orden de visualización debe ser al menos 1.") Integer displayOrder,

        @NotNull(message = "El peso (weight) es obligatorio.") @Min(value = 0, message = "El peso de la opción no puede ser negativo.") Integer weight,

        @NotBlank(message = "El texto de la opción no puede estar vacío.") @Size(max = 500, message = "El texto de la opción no puede superar los 500 caracteres.") String text) {
}
