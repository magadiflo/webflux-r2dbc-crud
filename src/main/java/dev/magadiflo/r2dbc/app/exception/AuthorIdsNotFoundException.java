package dev.magadiflo.r2dbc.app.exception;

public class AuthorIdsNotFoundException extends RuntimeException {
    public AuthorIdsNotFoundException() {
        super("Algunos IDs de autores no existen en el sistema");
    }
}
