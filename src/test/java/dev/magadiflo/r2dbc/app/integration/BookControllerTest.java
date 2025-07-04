package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dto.BookResponse;
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
    void throwsExceptionWhenSearchingForAnAuthorWhoseIdDoesNotExist() {
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
}
