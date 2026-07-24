package ar.edu.utn.frsf.talentmetricsAI_backend.dto.option;

public record OptionDetailResponse(
        Long id,
        Integer displayOrder,
        Integer weight,
        String text) {
}
