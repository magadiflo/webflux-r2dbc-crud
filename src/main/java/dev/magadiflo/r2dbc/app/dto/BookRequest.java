package dev.magadiflo.r2dbc.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


public record BookRequest(@NotBlank
                          @Size(min = 3)
                          String title,
                          @NotNull
                          LocalDate publicationDate,
                          Boolean onlineAvailability,
                          List<@NotNull Integer> authorIds) {
    // Constructor compacto
    public BookRequest {
        // El onlineAvailability es opcional. Si es null o false, será falso. Caso contrario será true
        onlineAvailability = Boolean.TRUE.equals(onlineAvailability);
    }

    public boolean hasNoAuthorIds() {
        return Objects.isNull(this.authorIds) || this.authorIds.isEmpty();
    }
}
