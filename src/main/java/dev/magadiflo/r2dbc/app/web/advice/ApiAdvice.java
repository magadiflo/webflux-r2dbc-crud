package dev.magadiflo.r2dbc.app.web.advice;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.utils.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApiAdvice {
    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<ResponseMessage<Void>>> exception(ApiException exception) {
        ResponseMessage<Void> message = ResponseMessage.<Void>builder()
                .message(exception.getMessage())
                .build();
        return Mono.just(new ResponseEntity<>(message, exception.getHttpStatus()));
    }
}
