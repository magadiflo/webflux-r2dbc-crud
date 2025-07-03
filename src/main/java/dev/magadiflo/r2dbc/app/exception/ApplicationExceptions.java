package dev.magadiflo.r2dbc.app.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationExceptions {

    public static <T> Mono<T> authorNotFound(Integer authorId) {
        return Mono.error(() -> new AuthorNotFoundException(authorId));
    }

    public static <T> Mono<T> bookNotFound(Integer bookId) {
        return Mono.error(() -> new BookNotFoundException(bookId));
    }

    public static <T> Mono<T> authorIdsNotFound() {
        return Mono.error(AuthorIdsNotFoundException::new);
    }

    public static <T> Mono<T> missingFirstName() {
        return Mono.error(() -> new InvalidInputException("El nombre es requerido"));
    }

    public static <T> Mono<T> missingLastName() {
        return Mono.error(() -> new InvalidInputException("El apellido es requerido"));
    }

    public static <T> Mono<T> missingBirthdate() {
        return Mono.error(() -> new InvalidInputException("La fecha de nacimiento es requerido"));
    }
}
