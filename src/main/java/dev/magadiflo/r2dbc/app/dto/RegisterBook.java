package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;
import java.util.List;

public record RegisterBook(String title,
                           LocalDate publicationDate,
                           Boolean onlineAvailability,
                           List<Integer> authors) {
    public RegisterBook {
        onlineAvailability = Boolean.TRUE.equals(onlineAvailability);
    }
}
