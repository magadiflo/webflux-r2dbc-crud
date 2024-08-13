package dev.magadiflo.r2dbc.app.model.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface IBookProjection {
    Integer getId();

    String getTitle();

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getPublicationDate();

    Boolean getOnlineAvailability();

    @JsonIgnore
    String getConcatAuthors();

    default List<String> getAuthors() {
        if (getConcatAuthors() == null || getConcatAuthors().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(getConcatAuthors().split(","));
    }
}
