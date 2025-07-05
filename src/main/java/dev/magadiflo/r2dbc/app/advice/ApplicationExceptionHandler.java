package dev.magadiflo.r2dbc.app.advice;

import dev.magadiflo.r2dbc.app.exception.AuthorIdsNotFoundException;
import dev.magadiflo.r2dbc.app.exception.AuthorNotFoundException;
import dev.magadiflo.r2dbc.app.exception.BookNotFoundException;
import dev.magadiflo.r2dbc.app.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    @ExceptionHandler(AuthorIdsNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(AuthorIdsNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Autor no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(BookNotFoundException exception) {
        ProblemDetail problemDetail = this.build(HttpStatus.NOT_FOUND, exception, detail -> {
            detail.setTitle("Libro no encontrado");
        });
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(WebExchangeBindException exception) {
        Map<String, List<String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                DefaultMessageSourceResolvable::getDefaultMessage,
                                Collectors.toList()
                        )
                ));
        ProblemDetail problemDetail = this.build(HttpStatus.BAD_REQUEST, exception, detail -> {
            detail.setTitle("El cuerpo de la petición contiene valores no válidos");
            detail.setDetail(exception.getReason());
            detail.setProperty("errors", errors);
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
