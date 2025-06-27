package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;

public record AuthorResponse(Integer id,
                             String firstName,
                             String lastName,
                             LocalDate birthdate) {
}
