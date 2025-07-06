package dev.magadiflo.r2dbc.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookUpdateRequest(@NotBlank
                                @Size(min = 3)
                                String title,
                                @NotNull
                                LocalDate publicationDate,
                                @NotNull
                                Boolean onlineAvailability) {
}
