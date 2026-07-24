package ar.edu.utn.frsf.talentmetricsAI_backend.dto.question;

import ar.edu.utn.frsf.talentmetricsAI_backend.dto.option.OptionRequest;
import ar.edu.utn.frsf.talentmetricsAI_backend.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;

public record QuestionRequest(
        @NotNull(message = "El ID del factor es obligatorio") Long factorId,

        @NotBlank(message = "El nombre de la pregunta no puede estar vacío") @Size(max = 255, message = "El nombre no puede superar los 255 caracteres") String name,

        @NotBlank(message = "El texto de la pregunta es obligatorio") String text,

        String description, // Este puede ser opcional, así que no le ponemos nada

        @NotNull(message = "El tipo de pregunta es obligatorio") QuestionType type,

        @NotNull(message = "Debe enviar al menos una opción") @Size(min = 2, message = "Una pregunta debe tener al menos 2 opciones") List<@Valid OptionRequest> options) {
}
