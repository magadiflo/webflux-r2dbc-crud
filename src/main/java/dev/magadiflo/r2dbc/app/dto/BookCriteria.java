package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;

public record BookCriteria(String query, LocalDate publicationDate) {
}
