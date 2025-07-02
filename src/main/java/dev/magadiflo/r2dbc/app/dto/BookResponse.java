package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;

public record BookResponse(Integer id,
                           String title,
                           LocalDate publicationDate,
                           Boolean onlineAvailability) {
}
