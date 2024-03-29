package dev.magadiflo.r2dbc.app.web.api;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.model.dto.AuthorFilter;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import dev.magadiflo.r2dbc.app.service.IAuthorService;
import dev.magadiflo.r2dbc.app.web.util.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors")
public class AuthorRestController {

    private final IAuthorService authorService;

    @GetMapping
    public Flux<Author> getAuthor(AuthorCriteria authorCriteria) {
        return this.authorService.findAll(authorCriteria);
    }

    @GetMapping(path = "/pages")
    public Mono<ResponseEntity<Page<IAuthorProjection>>> findAllPage(
            @RequestParam(name = "q", defaultValue = "", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "5", required = false) int size,
            @RequestParam(name = "sortBy", defaultValue = "authorId", required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc", required = false) String sortDirection) {

        String[] sortArray = sortBy.contains(",") ?
                Arrays.stream(sortBy.split(",")).map(String::trim).toArray(String[]::new) :
                new String[]{sortBy.trim()};

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortArray);
        Pageable pageable = PageRequest.of(page, size, sort);
        AuthorFilter authorFilter = new AuthorFilter(q);

        return this.authorService.findAllToPage(authorFilter, pageable)
                .flatMap(authorProjections -> Mono.just(ResponseEntity.ok(authorProjections)));
    }

    @GetMapping(path = "/{authorId}")
    public Mono<ResponseEntity<IAuthorProjection>> getAuthor(@PathVariable Integer authorId) throws ApiException {
        return this.authorService.findAuthorById(authorId)
                .flatMap(authorProjection -> Mono.just(ResponseEntity.ok(authorProjection)));
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> registerAuthor(@RequestBody RegisterAuthorDTO registerAuthorDTO) throws ApiException {
        return this.authorService.saveAuthor(registerAuthorDTO)
                .flatMap(authorId -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }

    @PutMapping(path = "/{authorId}")
    public Mono<ResponseEntity<ResponseMessage<IAuthorProjection>>> updateAuthor(@PathVariable Integer authorId,
                                                                                 @RequestBody UpdateAuthorDTO updateAuthorDTO) throws ApiException {
        return this.authorService.updateAuthor(authorId, updateAuthorDTO)
                .flatMap(authorProjection -> Mono.just(ResponseMessage.<IAuthorProjection>builder()
                        .message("Registro modificado")
                        .content(authorProjection)
                        .build())
                )
                .flatMap(msg -> Mono.just(new ResponseEntity<>(msg, HttpStatus.OK)));
    }

    @DeleteMapping(path = "/{authorId}")
    public Mono<ResponseEntity<Void>> deleteAuthor(@PathVariable Integer authorId) throws ApiException {
        return this.authorService.deleteAuthor(authorId)
                .then(Mono.just(new ResponseEntity<>(HttpStatus.OK)));
    }
}
