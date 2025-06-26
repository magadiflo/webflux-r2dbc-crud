package dev.magadiflo.r2dbc.app.proyection;

import java.time.LocalDate;
import java.util.Objects;

public interface AuthorProjection {
    String getFirstName();

    String getLastName();

    LocalDate getBirthdate();

    default String getFullName() {
        if (Objects.isNull(getFirstName()) || Objects.isNull(getLastName())) {
            return "";
        }
        return "%s %s".formatted(getFirstName(), getLastName());
    }
}
