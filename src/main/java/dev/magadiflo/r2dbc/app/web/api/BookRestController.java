package dev.magadiflo.r2dbc.app.web.api;

import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterBookDTO;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import dev.magadiflo.r2dbc.app.service.IBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookRestController {

    private final IBookService bookService;

    @GetMapping(path = "/pages")
    public Mono<ResponseEntity<Page<IBookProjection>>> findAllPage(
            @RequestParam(name = "q", defaultValue = "", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "5", required = false) int size,
            @RequestParam(name = "sortBy", defaultValue = "bookId", required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
            @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate publicationDate) {

        String[] sortArray = sortBy.contains(",") ?
                Arrays.stream(sortBy.split(",")).map(String::trim).toArray(String[]::new) :
                new String[]{sortBy.trim()};

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortArray);
        Pageable pageable = PageRequest.of(page, size, sort);
        BookCriteria bookCriteria = new BookCriteria(q, publicationDate);

        return this.bookService.findAllToPage(bookCriteria, pageable)
                .flatMap(bookProjections -> Mono.just(ResponseEntity.ok(bookProjections)));
    }

    @GetMapping(path = "/{bookId}")
    public Mono<ResponseEntity<IBookProjection>> getBook(@PathVariable Integer bookId) {
        return this.bookService.findBookById(bookId)
                .flatMap(bookProjection -> Mono.just(new ResponseEntity<>(bookProjection, HttpStatus.OK)));
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> registerBook(@RequestBody RegisterBookDTO registerBookDTO) {
        return this.bookService.saveBook(registerBookDTO)
                .doOnNext(bookId -> log.info("bookId: {}", bookId))
                .flatMap(bookId -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }

    @DeleteMapping(path = "/{bookId}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable Integer bookId) {
        return this.bookService.deleteBook(bookId)
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
