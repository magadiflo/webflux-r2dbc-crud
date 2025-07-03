package dev.magadiflo.r2dbc.app.exception;

public class BookNotFoundException extends RuntimeException {

    private static final String MESSAGE = "El libro [id=%d] no fue encontrado";

    public BookNotFoundException(Integer authorId) {
        super(MESSAGE.formatted(authorId));
    }
}
