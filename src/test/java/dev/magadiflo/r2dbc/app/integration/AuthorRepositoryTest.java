package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.entity.Author;
import dev.magadiflo.r2dbc.app.repository.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@DataR2dbcTest
class AuthorRepositoryTest extends AbstractTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void findAllAuthors() {
        this.authorRepository.findAllAuthorsByIdIn(List.of(2, 3))
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> {
                    Assertions.assertEquals(2, author.getId());
                    Assertions.assertEquals("Marco", author.getFirstName());
                    Assertions.assertEquals("Salvador", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("1995-06-09"), author.getBirthdate());
                })
                .assertNext(author -> {
                    Assertions.assertEquals(3, author.getId());
                    Assertions.assertEquals("Greys", author.getFirstName());
                    Assertions.assertEquals("Briones", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2001-10-03"), author.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void findAuthorById() {
        this.authorRepository.findAuthorById(1)
                .doOnNext(authorProjection -> log.info("{}", authorProjection))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Belén", authorProjection.getFirstName());
                    Assertions.assertEquals("Velez", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorProjection.getBirthdate());

                    // Comprobamos que el método por defecto de la proyección está funcionando
                    Assertions.assertEquals("Belén Velez", authorProjection.getFullName());
                })
                .verifyComplete();
    }

    @Test
    void findCountByQuery() {
        this.authorRepository.findCountByQuery("e")
                .as(StepVerifier::create)
                .assertNext(count -> Assertions.assertEquals(3, count))
                .verifyComplete();
    }

    @Test
    void findByQueryAndPageable() {
        this.authorRepository.findByQuery("e", PageRequest.of(0, 2))
                .doOnNext(authorProjection -> log.info("{}", authorProjection))
                .as(StepVerifier::create)
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Belén", authorProjection.getFirstName());
                    Assertions.assertEquals("Velez", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), authorProjection.getBirthdate());
                    //default method
                    Assertions.assertEquals("Belén Velez", authorProjection.getFullName());
                })
                .assertNext(authorProjection -> {
                    Assertions.assertEquals("Greys", authorProjection.getFirstName());
                    Assertions.assertEquals("Briones", authorProjection.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2001-10-03"), authorProjection.getBirthdate());
                    //default method
                    Assertions.assertEquals("Greys Briones", authorProjection.getFullName());
                })
                .verifyComplete();
    }

    @Test
    void saveAuthor() {
        Author author = Author.builder()
                .firstName("Susana")
                .lastName("Alvarado")
                .birthdate(LocalDate.parse("2000-11-07"))
                .build();
        this.authorRepository.saveAuthor(author)
                .doOnNext(affectedRows -> log.info("affectedRows: {}", affectedRows))
                .as(StepVerifier::create)
                .assertNext(affectedRows -> Assertions.assertEquals(1, affectedRows))
                .verifyComplete();

        this.authorRepository.count()
                .as(StepVerifier::create)
                .expectNext(5L)
                .verifyComplete();

        this.authorRepository.findById(5)
                .doOnNext(authorRetrieved -> log.info("{}", authorRetrieved))
                .as(StepVerifier::create)
                .assertNext(authorRetrieved -> {
                    Assertions.assertNotNull(authorRetrieved.getId());
                    Assertions.assertEquals("Susana", authorRetrieved.getFirstName());
                    Assertions.assertEquals("Alvarado", authorRetrieved.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2000-11-07"), authorRetrieved.getBirthdate());
                })
                .verifyComplete();

        this.authorRepository.deleteById(5)
                .then(this.authorRepository.count())
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void updateCustomer() {
        this.authorRepository.findById(1)
                .doOnNext(author -> log.info("{}", author))
                .doOnNext(author -> author.setFirstName("Belencita"))
                .flatMap(author -> this.authorRepository.updateAuthor(author))
                .as(StepVerifier::create)
                .assertNext(affectedRows -> Assertions.assertEquals(1, affectedRows))
                .verifyComplete();

        this.authorRepository.findById(1)
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> Assertions.assertEquals("Belencita", author.getFirstName()))
                .verifyComplete();
    }
}
