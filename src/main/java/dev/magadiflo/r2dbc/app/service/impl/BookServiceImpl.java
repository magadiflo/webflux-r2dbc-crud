package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import dev.magadiflo.r2dbc.app.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {


    @Override
    public Flux<BookResponse> findAllBooks() {
        return null;
    }

    @Override
    public Mono<BookProjection> findBookById(Integer bookId) {
        return null;
    }

    @Override
    public Mono<Page<BookProjection>> getAllBookAuthorsToPage(String query, int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public Mono<BookProjection> saveBook(BookRequest bookRequest) {
        return null;
    }

    @Override
    public Mono<BookProjection> updateBook(Integer bookId, BookRequest bookRequest) {
        return null;
    }

    @Override
    public Mono<Void> deleteBook(Integer bookId) {
        return null;
    }
}
