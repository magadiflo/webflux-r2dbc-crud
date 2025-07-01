package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dao.BookAuthorDao;
import dev.magadiflo.r2dbc.app.dao.impl.BookAuthorDaoImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.time.LocalDate;

@Slf4j
@Import(BookAuthorDaoImpl.class)
@DataR2dbcTest
class BookAuthorDaoImplTest extends AbstractTest {

    @Autowired
    private BookAuthorDao bookAuthorDao;

    @Test
    void countBookAuthorByCriteria() {
    }

    @Test
    void saveBookAuthor() {
    }

    @Test
    void saveAllBookAuthor() {
    }

    @Test
    void existBookAuthorByBookId() {
    }

    @Test
    void existBookAuthorByAuthorId() {
    }

    @Test
    void deleteBookAuthorByBookId() {
    }

    @Test
    void deleteBookAuthorByAuthorId() {
    }

    @Test
    void findByBookId() {
        this.bookAuthorDao.findByBookId(1)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertEquals("Belén Velez, Marco Salvador", bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals("Belén Velez", bookProjection.authorNames().get(0));
                    Assertions.assertEquals("Marco Salvador", bookProjection.authorNames().get(1));
                })
                .verifyComplete();
    }

    @Test
    void findByBookIdWhenIdNotExists() {
        this.bookAuthorDao.findByBookId(3)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findAllBookAuthorByBookId() {
    }

    @Test
    void findAllToPage() {
    }
}