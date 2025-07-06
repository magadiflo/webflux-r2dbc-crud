package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dto.BookAuthorUpdateRequest;
import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.dto.BookUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AutoConfigureWebTestClient //Para autoconfigurar WebTestClient
@SpringBootTest
class BookControllerTest extends AbstractTest {

    private static final String BOOKS_URI = "/api/v1/books";

    @Autowired
    private WebTestClient client;

    @Test
    void getBooks() {
        this.client.get()
                .uri(BOOKS_URI.concat("/stream"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(BookResponse.class)
                .getResponseBody()
                .doOnNext(bookResponse -> log.info("{}", bookResponse))
                .as(StepVerifier::create)
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(1, bookResponse.id());
                    Assertions.assertEquals("Los ríos profundos", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(2, bookResponse.id());
                    Assertions.assertEquals("La ciudad y los perros", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAuthor() {
        this.client.get()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Los ríos profundos")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.parse("1999-01-15"))
                .jsonPath("$.onlineAvailability").isEqualTo(true)
                .jsonPath("$.authorNames.length()").isNotEmpty()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador");
    }

    @Test
    void throwsExceptionWhenSearchingForAnBookWhoseIdDoesNotExist() {
        this.client.get()
                .uri(BOOKS_URI.concat("/{bookId}"), 10)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El libro [id=10] no fue encontrado");
    }

    @Test
    void getPaginatedBooks() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("pageNumber", 0)
                        .queryParam("pageSize", 2)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(4);
    }

    @Test
    void getPaginatedBooksWithAuthors() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("query", "ro")
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
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.number").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(3);
    }

    @Test
    void getPaginatedBooksWithoutAuthors() {
        this.client.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_URI.concat("/paginated"))
                        .queryParam("publicationDate", LocalDate.parse("1988-07-15"))
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
    void givenValidBookRequest_whenSaveBook_thenReturnsCreatedBookProjection() {
        BookRequest bookRequest = new BookRequest("Kubernetes", LocalDate.now(), null, List.of(2, 4));
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Kubernetes")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.now())
                .jsonPath("$.onlineAvailability").isEqualTo(false)
                .jsonPath("$.authorNames.length()").isNotEmpty()
                .jsonPath("$.authorNames[0]").isEqualTo("Marco Salvador")
                .jsonPath("$.authorNames[1]").isEqualTo("Luis Sánchez");
    }

    @Test
    void givenInvalidBookRequest_whenSaveBook_thenReturnsValidationErrors() {
        List<Integer> authorIds = new ArrayList<>();
        authorIds.add(1);
        authorIds.add(null);
        authorIds.add(4);
        BookRequest bookRequest = new BookRequest(" ", null, false, authorIds);
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.errors['authorIds[1]']").isArray()
                .jsonPath("$.errors['authorIds[1]'][0]").isEqualTo("must not be null")
                .jsonPath("$.errors.title").isArray()
                .jsonPath("$.errors.title[0]").exists()
                .jsonPath("$.errors.title[1]").isNotEmpty()
                .jsonPath("$.errors.publicationDate").isArray()
                .jsonPath("$.errors.publicationDate[0]").isEqualTo("must not be null");
    }

    @Test
    void givenBookRequestWithNonExistingAuthors_whenSaveBook_thenThrowsAuthorIdsNotFoundException() {
        BookRequest bookRequest = new BookRequest("Kubernetes", LocalDate.now(), true, List.of(2, 10, 4));
        this.client.post()
                .uri(BOOKS_URI)
                .bodyValue(bookRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Autor no encontrado")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("Algunos IDs de autores no existen en el sistema");
    }

    @Test
    void givenValidBookRequest_whenUpdateBook_thenReturnsUpdatedBookProjection() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Spring WebFlux")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.now())
                .jsonPath("$.onlineAvailability").isEqualTo(false)
                .jsonPath("$.authorNames").isArray()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador");
    }

    @Test
    void givenInvalidBookRequest_whenUpdateBook_thenReturnsValidationErrors() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("", null, null);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 1)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors.title").isArray()
                .jsonPath("$.errors.title.length()").isEqualTo(2)
                .jsonPath("$.errors.publicationDate").isArray()
                .jsonPath("$.errors.publicationDate.length()").isEqualTo(1)
                .jsonPath("$.errors.onlineAvailability").isArray()
                .jsonPath("$.errors.onlineAvailability.length()").isEqualTo(1);
    }

    @Test
    void givenNonIntegerBookId_whenUpdateBook_thenReturnsBadRequest() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), "abc")
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Error de formato de la petición")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void givenNonExistingBookId_whenUpdateBook_thenReturnsNotFound() {
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("Spring WebFlux", LocalDate.now(), false);
        this.client.put()
                .uri(BOOKS_URI.concat("/{bookId}"), 10)
                .bodyValue(bookUpdateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void givenValidAuthorIds_whenUpdateBookAuthors_thenReturnsUpdatedBookProjection() {
        var request = new BookAuthorUpdateRequest(List.of(1, 2, 3));
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 1)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Los ríos profundos")
                .jsonPath("$.publicationDate").isEqualTo(LocalDate.parse("1999-01-15"))
                .jsonPath("$.onlineAvailability").isEqualTo(true)
                .jsonPath("$.authorNames").isArray()
                .jsonPath("$.authorNames[0]").isEqualTo("Belén Velez")
                .jsonPath("$.authorNames[1]").isEqualTo("Marco Salvador")
                .jsonPath("$.authorNames[2]").isEqualTo("Greys Briones");
    }

    @Test
    void givenNonExistingBookId_whenUpdateBookAuthors_thenReturnsNotFound() {
        var request = new BookAuthorUpdateRequest(List.of(1, 2, 3));
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 10)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("Libro no encontrado")
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void givenNullAuthorIdInList_whenUpdateBookAuthors_thenReturnsValidationErrors() {
        List<Integer> authorIds = new ArrayList<>();
        authorIds.add(1);
        authorIds.add(null);
        authorIds.add(4);
        var request = new BookAuthorUpdateRequest(authorIds);
        this.client.patch()
                .uri(BOOKS_URI.concat("/{bookId}/authors"), 1)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("{}", new String(Objects.requireNonNull(result.getResponseBody()))))
                .jsonPath("$.title").isEqualTo("El cuerpo de la petición contiene valores no válidos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.detail").isEqualTo("Validation failure")
                .jsonPath("$.errors['authorIds[1]']").isArray()
                .jsonPath("$.errors['authorIds[1]'][0]").isEqualTo("must not be null");
    }
}
