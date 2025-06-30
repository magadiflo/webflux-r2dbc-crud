package dev.magadiflo.r2dbc.app.validator;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.exception.ApplicationExceptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestValidator {

    public static UnaryOperator<Mono<AuthorRequest>> validate() {
        return authorRequestMono -> authorRequestMono
                .filter(hasFirstName())
                .switchIfEmpty(ApplicationExceptions.missingFirstName())
                .filter(hasLastName())
                .switchIfEmpty(ApplicationExceptions.missingLastName())
                .filter(hasBirthdate())
                .switchIfEmpty(ApplicationExceptions.missingBirthdate());
    }

    private static Predicate<AuthorRequest> hasFirstName() {
        return authorRequest -> Objects.nonNull(authorRequest.firstName())
                                && !authorRequest.firstName().trim().isEmpty();
    }

    private static Predicate<AuthorRequest> hasLastName() {
        return authorRequest -> Objects.nonNull(authorRequest.lastName())
                                && !authorRequest.lastName().trim().isEmpty();
    }

    private static Predicate<AuthorRequest> hasBirthdate() {
        return authorRequest -> Objects.nonNull(authorRequest.birthdate());
    }
}
