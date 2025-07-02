package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.exception.AuthorNotFoundException;
import dev.magadiflo.r2dbc.app.proyection.AuthorProjection;
import dev.magadiflo.r2dbc.app.service.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
class AuthorServiceImplTest extends AbstractTest {

    @Autowired
    private AuthorService authorService;

    @Test
    void findAllAuthors() {
        this.authorService.findAllAuthors()
                .doOnNext(authorResponse -> log.info("{}", authorResponse))
                .as(StepVerifier::create)
                .assertNext(authorResponse -> {
                    Assertions.assertEquals(1, authorResponse.id());
                    Assertions.assertEquals("Belén", authorResponse.firstName());
                    Assertions.assertEquals("Velez", authorResponse.lastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorResponse.birthdate());
                })
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findAuthorById() {
        this.authorService.findAuthorById(1)
                .doOnNext(authorResponse -> log.info("{}", authorResponse))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Belén", authorProjection.getFirstName());
                    Assertions.assertEquals("Velez", authorProjection.getLastName());
                    Assertions.assertEquals("Belén Velez", authorProjection.getFullName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorProjection.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void findAuthorWhenIdNotExist() {
        this.authorService.findAuthorById(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El author [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void testGetAllAuthorsToPage_withData() {
        String query = "o";
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<AuthorProjection>> result = authorService.getAllAuthorsToPage(query, pageNumber, pageSize)
                .doOnNext(authorProjections -> log.info("{}", authorProjections.getContent()));

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertNotNull(page);
                    Assertions.assertEquals(2, page.getContent().size());
                    Assertions.assertEquals(0, page.getNumber());
                    Assertions.assertEquals(2, page.getSize());
                    Assertions.assertTrue(page.getTotalElements() >= 2);

                    AuthorProjection firstAuthor = page.getContent().get(0);
                    Assertions.assertNotNull(firstAuthor.getFirstName());
                    Assertions.assertNotNull(firstAuthor.getLastName());
                    Assertions.assertNotNull(firstAuthor.getFullName());
                    Assertions.assertNotNull(firstAuthor.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void testGetAllAuthorsToPage_noData() {
        String query = "no-existente";
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<AuthorProjection>> result = authorService.getAllAuthorsToPage(query, pageNumber, pageSize);

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertNotNull(page);
                    Assertions.assertEquals(0, page.getContent().size());
                    Assertions.assertEquals(0, page.getTotalElements());
                })
                .verifyComplete();
    }

    @Test
    void saveAuthor() {
        this.authorService.saveAuthor(new AuthorRequest("Vanesa", "Flores", LocalDate.parse("2005-05-15")))
                .as(StepVerifier::create)
                .assertNext(affectedRows -> Assertions.assertEquals(1, affectedRows))
                .verifyComplete();
    }

    @Test
    void updateAuthor() {
        this.authorService.updateAuthor(1, new AuthorRequest("Vanesa", "Flores", LocalDate.parse("2005-05-15")))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Vanesa", authorProjection.getFirstName());
                    Assertions.assertEquals("Flores", authorProjection.getLastName());
                    Assertions.assertEquals("Vanesa Flores", authorProjection.getFullName());
                    Assertions.assertEquals(LocalDate.parse("2005-05-15"), authorProjection.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void throwErrorWhenUpdateAuthorWithIdThatDoesNotExist() {
        this.authorService.updateAuthor(5, new AuthorRequest("Vanesa", "Flores", LocalDate.parse("2005-05-15")))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El author [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void deleteAuthorWithoutAssociatedBooks() {
        this.authorService.deleteAuthor(4)
                .as(StepVerifier::create)
                .assertNext(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    void deleteAuthorWithAssociatedBooks() {
        this.authorService.deleteAuthor(2)
                .as(StepVerifier::create)
                .assertNext(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    void throwErrorWhenDeleteAuthorWithIdThatDoesNotExist() {
        this.authorService.deleteAuthor(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El author [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }
}
