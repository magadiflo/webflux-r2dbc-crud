package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record BookRequest(String title,
                          LocalDate publicationDate,
                          Boolean onlineAvailability,
                          List<Integer> authorIds) {
    public boolean hasNoAuthorIds() {
        return Objects.isNull(this.authorIds) || this.authorIds.isEmpty();
    }
}
