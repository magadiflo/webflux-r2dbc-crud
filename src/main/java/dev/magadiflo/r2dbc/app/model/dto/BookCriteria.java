package dev.magadiflo.r2dbc.app.model.dto;

import java.time.LocalDate;

public record BookCriteria(String q, LocalDate publicationDate) {
}
