package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dao.AuthorDao;
import dev.magadiflo.r2dbc.app.dao.impl.AuthorDaoImpl;
import dev.magadiflo.r2dbc.app.dto.AuthorCriteria;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.time.LocalDate;

@Slf4j
@Import(AuthorDaoImpl.class)
@DataR2dbcTest
class AuthorDaoImplTest extends AbstractTest {

    @Autowired
    private AuthorDao authorDao;

    @Test
    void findAuthorByCriteria() {
        this.authorDao.findAuthorByCriteria(new AuthorCriteria(" ", "o"))
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> {
                    Assertions.assertEquals(2, author.getId());
                    Assertions.assertEquals("Marco", author.getFirstName());
                    Assertions.assertEquals("Salvador", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("1995-06-09"), author.getBirthdate());
                }).assertNext(author -> {
                    Assertions.assertEquals(3, author.getId());
                    Assertions.assertEquals("Greys", author.getFirstName());
                    Assertions.assertEquals("Briones", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2001-10-03"), author.getBirthdate());
                })
                .verifyComplete();
    }

    @Test
    void findAuthorByCriteriaWithNoFilters() {
        this.authorDao.findAuthorByCriteria(new AuthorCriteria(null, null))
                .doOnNext(author -> log.info("{}", author))
                .as(StepVerifier::create)
                .assertNext(author -> {
                    Assertions.assertEquals(1, author.getId());
                    Assertions.assertEquals("Belén", author.getFirstName());
                    Assertions.assertEquals("Velez", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("2006-06-15"), author.getBirthdate());
                })
                .assertNext(author -> {
                    Assertions.assertEquals(2, author.getId());
                    Assertions.assertEquals("Marco", author.getFirstName());
                    Assertions.assertEquals("Salvador", author.getLastName());
                    Assertions.assertEquals(LocalDate.parse("1995-06-09"), author.getBirthdate());
                })
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAuthorByCriteriaWithNoMatching() {
        this.authorDao.findAuthorByCriteria(new AuthorCriteria("Ronaldinho", "Gaucho"))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}