package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;
import java.util.List;

public record BookRequest(String title,
                          LocalDate publicationDate,
                          Boolean onlineAvailability,
                          List<Integer> authorIds) {
    public BookRequest { // constructor compacto
        onlineAvailability = Boolean.TRUE.equals(onlineAvailability); // si onlineAvailability es null o false dará false, caso contrario true
    }
}
