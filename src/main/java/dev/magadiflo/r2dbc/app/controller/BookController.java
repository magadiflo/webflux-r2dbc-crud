package dev.magadiflo.r2dbc.app.controller;

import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.dto.BookUpdateRequest;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import dev.magadiflo.r2dbc.app.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<BookResponse>>> getBooks() {
        return Mono.fromSupplier(() -> ResponseEntity.ok(this.bookService.findAllBooks()));
    }

    @GetMapping(path = "/{bookId}")
    public Mono<ResponseEntity<BookProjection>> getBook(@PathVariable Integer bookId) {
        return this.bookService.findBookById(bookId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/paginated")
    public Mono<ResponseEntity<Page<BookProjection>>> getPaginatedBooks(@RequestParam(required = false) LocalDate publicationDate,
                                                                        @RequestParam(required = false, defaultValue = "") String query,
                                                                        @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                        @RequestParam(required = false, defaultValue = "5") int pageSize) {
        return this.bookService.findBooksWithAuthorsByCriteria(query, publicationDate, pageNumber, pageSize)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<BookProjection>> saveBook(@Valid @RequestBody Mono<BookRequest> bookRequestMono) {
        return bookRequestMono
                .flatMap(this.bookService::saveBook)
                .map(bookProjection -> ResponseEntity.status(HttpStatus.CREATED).body(bookProjection));
    }

    @PutMapping(path = "/{bookId}")
    public Mono<ResponseEntity<BookProjection>> updateBook(@PathVariable Integer bookId,
                                                           @Valid @RequestBody Mono<BookUpdateRequest> bookUpdateRequestMono) {
        return bookUpdateRequestMono
                .flatMap(bookUpdateRequest -> this.bookService.updateBook(bookId, bookUpdateRequest))
                .map(ResponseEntity::ok);
    }
}
