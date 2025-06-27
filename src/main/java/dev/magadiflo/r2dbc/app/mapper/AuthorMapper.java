package dev.magadiflo.r2dbc.app.mapper;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import dev.magadiflo.r2dbc.app.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorMapper {
    AuthorResponse toAuthorResponse(Author author);

    Author toAuthor(AuthorRequest authorRequest);
}
