package dev.magadiflo.r2dbc.app.exception;

public class AuthorNotFoundException extends RuntimeException {

    private static final String MESSAGE = "El author [id=%d] no fue encontrado";

    public AuthorNotFoundException(Integer authorId) {
        super(MESSAGE.formatted(authorId));
    }
}
