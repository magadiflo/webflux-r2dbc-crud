package dev.magadiflo.r2dbc.app.advice;

import dev.magadiflo.r2dbc.app.exception.AuthorNotFoundException;
import dev.magadiflo.r2dbc.app.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(AuthorNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(AuthorNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Autor no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(InvalidInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(InvalidInputException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("Entrada no válida");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDecodingException(ServerWebInputException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("Error de formato en el cuerpo de la petición");
            log.info("{}", exception.getMostSpecificCause().getMessage());
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(Exception exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.INTERNAL_SERVER_ERROR, exception, detail -> {
            detail.setTitle("Se produjo un error interno en el servidor");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }

    private ProblemDetail build(HttpStatus status, Exception exception, Consumer<ProblemDetail> detailConsumer) {
        log.info("{}", exception.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        detailConsumer.accept(problemDetail);
        return problemDetail;
    }
}
