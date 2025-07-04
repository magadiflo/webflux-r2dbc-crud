package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;

public record BookUpdateRequest(String title,
                                LocalDate publicationDate,
                                Boolean onlineAvailability) {
}
