package dev.magadiflo.r2dbc.app.utils;


import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.RegisterBookDTO;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import dev.magadiflo.r2dbc.app.persistence.entity.Book;
import dev.magadiflo.r2dbc.app.persistence.entity.BookAuthor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookMapper {

    private final ModelMapper modelMapper;

    public Mono<Book> toBook(RegisterBookDTO dto) {
        try {
            Book book = modelMapper.map(dto, Book.class);
            return Mono.just(book);
        } catch (Exception e) {
            log.error("Error en mapeo para registrar book:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }

    public List<BookAuthor> toBookAuthorList(List<Author> authors, Integer bookId) {
        return authors.stream()
                .map(author -> BookAuthor
                        .builder()
                        .bookId(bookId)
                        .authorId(author.getId())
                        .build()
                )
                .toList();
    }
}
