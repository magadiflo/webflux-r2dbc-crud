package dev.magadiflo.r2dbc.app.utils;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorMapper {

    private final ModelMapper modelMapper;

    public Mono<Author> toAuthor(RegisterAuthorDTO registerAuthor) {
        try {
            Author author = this.modelMapper.map(registerAuthor, Author.class);
            return Mono.just(author);
        } catch (Exception e) {
            log.error("Error en mapeo para registrar author:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }

    public Mono<Author> toAuthor(UpdateAuthorDTO registerAuthor, Integer authorId) {
        try {
            Author author = this.modelMapper.map(registerAuthor, Author.class);
            author.setId(authorId);
            return Mono.just(author);
        } catch (Exception e) {
            log.error("Error en mapeo para actualizar author:: {}", e.getMessage());
            return Mono.error(new ApiException("Error al mapear entidad Author", HttpStatus.BAD_REQUEST));
        }
    }
}
