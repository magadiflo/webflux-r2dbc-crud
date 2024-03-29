package dev.magadiflo.r2dbc.app.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UpdateAuthorDTO(String firstName,
                              String lastName,
                              @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthdate) {
}
