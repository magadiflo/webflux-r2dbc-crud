package dev.magadiflo.r2dbc.app.model.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

@JsonPropertyOrder(value = {"id", "firstName", "lastName", "fullName", "birthdate"})
public interface IAuthorProjection {
    Integer getId();

    String getFirstName();

    String getLastName();

    default String getFullName() {
        if (getFirstName() == null || getLastName() == null) {
            return "";
        }
        return "%s %s".formatted(getFirstName(), getLastName());
    }

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getBirthdate();
}
