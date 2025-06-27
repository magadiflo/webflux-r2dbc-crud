package dev.magadiflo.r2dbc.app.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationExceptions {

    public static <T> Mono<T> authorNotFound(Integer authorId) {
        return Mono.error(() -> new AuthorNotFoundException(authorId));
    }
}
