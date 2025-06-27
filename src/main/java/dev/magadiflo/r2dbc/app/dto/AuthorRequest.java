package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;

public record AuthorRequest(String firstName,
                            String lastName,
                            LocalDate birthdate) {
}
