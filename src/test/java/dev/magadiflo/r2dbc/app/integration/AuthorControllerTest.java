package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@AutoConfigureWebTestClient //Para autoconfigurar WebTestClient
@SpringBootTest
class AuthorControllerTest extends AbstractTest {

    private static final String AUTHORS_URI = "/api/v1/authors";

    @Autowired
    private WebTestClient client;

    @Test
    void getAuthors() {
        this.client.get()
                .uri(AUTHORS_URI.concat("/stream"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(AuthorResponse.class)
                .getResponseBody()
                .doOnNext(authorResponse -> log.info("{}", authorResponse))
                .as(StepVerifier::create)
                .assertNext(authorResponse -> {
                    Assertions.assertEquals(1, authorResponse.id());
                    Assertions.assertEquals("Belén", authorResponse.firstName());
                    Assertions.assertEquals("Velez", authorResponse.lastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorResponse.birthdate());
                })
                .assertNext(authorResponse -> {
                    Assertions.assertEquals(2, authorResponse.id());
                    Assertions.assertEquals("Marco", authorResponse.firstName());
                    Assertions.assertEquals("Salvador", authorResponse.lastName());
                    Assertions.assertEquals(LocalDate.parse("1995-06-09"), authorResponse.birthdate());
                })
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAuthor() {
        this.client.get()
                .uri(AUTHORS_URI.concat("/{authorId}"), 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.firstName").isEqualTo("Belén")
                .jsonPath("$.lastName").isEqualTo("Velez")
                .jsonPath("$.fullName").isEqualTo("Belén Velez")
                .jsonPath("$.birthdate").isEqualTo(LocalDate.parse("2006-06-15"));
    }

    @Test
    void throwsExceptionWhenSearchingForAnAuthorWhoseIdDoesNotExist() {
        this.client.get()
                .uri(AUTHORS_URI.concat("/{authorId}"), 10)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Autor no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El author [id=10] no fue encontrado");
    }

    @Test
    void getPaginatedAuthors() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(AUTHORS_URI.concat("/paginated"))
                        .queryParam("query", "")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 5)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(4)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(4);
    }

    @Test
    void getPaginatedAuthorsWithSearchTerm() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(AUTHORS_URI.concat("/paginated"))
                        .queryParam("query", "rio")
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 5)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void saveAuthor() {
        AuthorRequest authorRequest = new AuthorRequest("Kathy", "Zeñas", LocalDate.parse("1995-08-26"));
        this.client.post()
                .uri(AUTHORS_URI)
                .bodyValue(authorRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .isEmpty();
    }

    @Test
    void validateEntryToSaveAuthor() {
        AuthorRequest authorRequest1 = new AuthorRequest(" ", "Zeñas", LocalDate.parse("1995-08-26"));
        this.validateEntryPost(authorRequest1, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("El nombre es requerido");

        AuthorRequest authorRequest2 = new AuthorRequest("Alisson", null, LocalDate.parse("1995-08-26"));
        this.validateEntryPost(authorRequest2, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("El apellido es requerido");

        AuthorRequest authorRequest3 = new AuthorRequest("Alisson", "Zeñas", null);
        this.validateEntryPost(authorRequest3, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("La fecha de nacimiento es requerido");
    }

    @Test
    void updateAuthor() {
        AuthorRequest authorRequest = new AuthorRequest("Kathy", "Zeñas", LocalDate.parse("1995-08-26"));
        this.client.put()
                .uri(AUTHORS_URI.concat("/{authorId}"), 1)
                .bodyValue(authorRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.firstName").isEqualTo("Kathy")
                .jsonPath("$.lastName").isEqualTo("Zeñas")
                .jsonPath("$.fullName").isEqualTo("Kathy Zeñas")
                .jsonPath("$.birthdate").isEqualTo(LocalDate.parse("1995-08-26"));
    }

    @Test
    void throwsAnExceptionWhenUpdatingAnAuthorWhoseIdDoesNotExist() {
        AuthorRequest authorRequest = new AuthorRequest("Kathy", "Zeñas", LocalDate.parse("1995-08-26"));
        this.client.put()
                .uri(AUTHORS_URI.concat("/{authorId}"), 10)
                .bodyValue(authorRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Autor no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El author [id=10] no fue encontrado");
    }

    @Test
    void validateEntryToUpdateAuthor() {
        AuthorRequest authorRequest1 = new AuthorRequest(null, "Zeñas", LocalDate.parse("1995-08-26"));
        this.validateEntryPut(authorRequest1, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("El nombre es requerido");

        AuthorRequest authorRequest2 = new AuthorRequest("Alisson", "", LocalDate.parse("1995-08-26"));
        this.validateEntryPut(authorRequest2, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("El apellido es requerido");

        AuthorRequest authorRequest3 = new AuthorRequest("Alisson", "Zeñas", null);
        this.validateEntryPut(authorRequest3, HttpStatus.BAD_REQUEST)
                .jsonPath("$.title").isEqualTo("Entrada no válida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("La fecha de nacimiento es requerido");
    }

    @Test
    void deleteAuthor() {
        this.client.delete()
                .uri(AUTHORS_URI.concat("/{authorId}"), 4)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    void throwsAnExceptionWhenDeletingAnAuthorWhoseIdDoesNotExist() {
        this.client.delete()
                .uri(AUTHORS_URI.concat("/{authorId}"), 5)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Autor no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El author [id=5] no fue encontrado");
    }

    private WebTestClient.BodyContentSpec validateEntryPost(AuthorRequest authorRequest, HttpStatus expectedStatus) {
        return this.client.post()
                .uri(AUTHORS_URI)
                .bodyValue(authorRequest)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))));
    }

    private WebTestClient.BodyContentSpec validateEntryPut(AuthorRequest authorRequest, HttpStatus expectedStatus) {
        return this.client.put()
                .uri(AUTHORS_URI.concat("/{authorId}"), 1)
                .bodyValue(authorRequest)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))));
    }
}
