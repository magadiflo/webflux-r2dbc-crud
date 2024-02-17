package dev.magadiflo.r2dbc.app.model.vo;

import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@ToString
public class BookVO implements IBookProjection {
    private Integer id;
    private String title;
    private LocalDate publicationDate;
    private String concatAuthors;
    private Boolean onlineAvailability;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public LocalDate getPublicationDate() {
        return this.publicationDate;
    }

    @Override
    public Boolean getOnlineAvailability() {
        return this.onlineAvailability;
    }

    @Override
    public String getConcatAuthors() {
        return this.concatAuthors;
    }
}
