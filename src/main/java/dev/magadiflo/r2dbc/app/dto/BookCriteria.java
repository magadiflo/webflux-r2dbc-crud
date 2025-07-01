package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;
import java.util.Objects;

public record BookCriteria(String query, LocalDate publicationDate) {
    public boolean hasQuery() {
        return Objects.nonNull(this.query) && !this.query.isBlank();
    }

    public boolean hasPublicationDate() {
        return Objects.nonNull(this.publicationDate);
    }
}
