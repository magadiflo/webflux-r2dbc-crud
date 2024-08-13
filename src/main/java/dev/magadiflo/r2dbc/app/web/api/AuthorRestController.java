package dev.magadiflo.r2dbc.app.web.api;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import dev.magadiflo.r2dbc.app.service.IAuthorService;
import dev.magadiflo.r2dbc.app.utils.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors")
public class AuthorRestController {

    private final IAuthorService authorService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Author>>> findAllAuthors(AuthorCriteria authorCriteria) {
        Flux<Author> authorFlux = this.authorService.findAll(authorCriteria);
        return authorFlux
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(authorFlux));
                    }
                    return Mono.just(ResponseEntity.noContent().build());
                });
    }

    @GetMapping(path = "/pages")
    public Mono<ResponseEntity<Page<IAuthorProjection>>> findAllPage(@RequestParam(name = "query", defaultValue = "", required = false) String query,
                                                                     @RequestParam(name = "page", defaultValue = "0", required = false) int pageNumber,
                                                                     @RequestParam(name = "size", defaultValue = "5", required = false) int pageSize) {
        return this.authorService.findAllToPage(query, pageNumber, pageSize)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{authorId}")
    public Mono<ResponseEntity<IAuthorProjection>> getAuthor(@PathVariable Integer authorId) throws ApiException {
        return this.authorService.findAuthorById(authorId)
                .flatMap(authorProjection -> Mono.just(ResponseEntity.ok(authorProjection)));
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> registerAuthor(@RequestBody RegisterAuthorDTO registerAuthorDTO) throws ApiException {
        return this.authorService.saveAuthor(registerAuthorDTO)
                .flatMap(affectedRows -> Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
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
                .map(wasDeleted -> ResponseEntity.noContent().build());
    }
}
