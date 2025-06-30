package dev.magadiflo.r2dbc.app.controller;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import dev.magadiflo.r2dbc.app.proyection.AuthorProjection;
import dev.magadiflo.r2dbc.app.service.AuthorService;
import dev.magadiflo.r2dbc.app.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<AuthorResponse>>> getAuthors() {
        return Mono.fromSupplier(() -> ResponseEntity.ok(this.authorService.findAllAuthors()));
    }

    @GetMapping(path = "/{authorId}")
    public Mono<ResponseEntity<AuthorProjection>> getAuthor(@PathVariable Integer authorId) {
        return this.authorService.findAuthorById(authorId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/paginated")
    public Mono<ResponseEntity<Page<AuthorProjection>>> getPaginatedAuthors(@RequestParam(required = false, defaultValue = "") String query,
                                                                            @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                            @RequestParam(required = false, defaultValue = "5") int pageSize) {
        return this.authorService.getAllAuthorsToPage(query, pageNumber, pageSize)
                .map(ResponseEntity::ok);

    }

    @PostMapping
    public Mono<ResponseEntity<Void>> saveAuthor(@RequestBody Mono<AuthorRequest> authorRequestMono) {
        return authorRequestMono
                .transform(RequestValidator.validate())
                .flatMap(this.authorService::saveAuthor)
                .map(affectedRows -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @PutMapping(path = "/{authorId}")
    public Mono<ResponseEntity<AuthorProjection>> updateAuthor(@PathVariable Integer authorId,
                                                               @RequestBody Mono<AuthorRequest> authorRequestMono) {
        return authorRequestMono
                .transform(RequestValidator.validate())
                .flatMap(authorRequest -> this.authorService.updateAuthor(authorId, authorRequest))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{authorId}")
    public Mono<ResponseEntity<Void>> deleteAuthor(@PathVariable Integer authorId) {
        return this.authorService.deleteAuthor(authorId)
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
