package dev.magadiflo.r2dbc.app.dto;

import java.time.LocalDate;
import java.util.List;

public record BookRequest(String title,
                          LocalDate publicationDate,
                          Boolean onlineAvailability,
                          List<Integer> authorIds) {
}
