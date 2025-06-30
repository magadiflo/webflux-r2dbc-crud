package dev.magadiflo.r2dbc.app.proyection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record BookProjection(String title,
                             LocalDate publicationDate,
                             Boolean onlineAvailability,
                             @JsonIgnore
                             String authors) {

    @JsonProperty
    public List<String> authorNames() {
        if (Objects.isNull(this.authors) || this.authors.isBlank()) {
            return List.of();
        }
        return Arrays.stream(this.authors.split(","))
                .map(String::trim)
                .toList();
    }
}
