package dev.magadiflo.r2dbc.app.mapper;

import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.dto.BookUpdateRequest;
import dev.magadiflo.r2dbc.app.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    BookResponse toBookResponse(Book book);

    Book toBook(BookRequest bookRequest);

    @Mapping(target = "id", ignore = true)
    Book toBookUpdate(@MappingTarget Book book, BookUpdateRequest bookUpdateRequest);
}
