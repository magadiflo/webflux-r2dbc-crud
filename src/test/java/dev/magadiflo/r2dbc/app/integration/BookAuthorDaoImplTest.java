package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dao.BookAuthorDao;
import dev.magadiflo.r2dbc.app.dao.impl.BookAuthorDaoImpl;
import dev.magadiflo.r2dbc.app.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.entity.BookAuthor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

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
        this.bookAuthorDao.saveBookAuthor(new BookAuthor(4, 3))
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void saveBookAuthorExpectError() {
        this.bookAuthorDao.saveBookAuthor(new BookAuthor(1, 1))
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void saveAllBookAuthor() {
        this.bookAuthorDao.saveAllBookAuthor(List.of(
                        new BookAuthor(3, 1),
                        new BookAuthor(4, 1),
                        new BookAuthor(2, 2)
                ))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void saveAllBookAuthorExpectError() {
        this.bookAuthorDao.saveAllBookAuthor(List.of(
                        new BookAuthor(3, 1),
                        new BookAuthor(4, 1),
                        new BookAuthor(1, 1)
                ))
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void existBookAuthorByBookId() {
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void notExistBookAuthorByBookId() {
        this.bookAuthorDao.existBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existBookAuthorByAuthorId() {
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void notExistBookAuthorByAuthorId() {
        this.bookAuthorDao.existBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookId() {
        // Verificamos que exista por el BookId
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .verifyComplete();

        // Verificamos que ya no existe
        this.bookAuthorDao.existBookAuthorByBookId(1)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookIdWhenBookIdNotExist() {
        // Verificamos que no existe
        this.bookAuthorDao.existBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByBookId(3)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByAuthorId() {
        // Verificamos que exista por el AuthorId
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .verifyComplete();

        // Verificamos que ya no existe
        this.bookAuthorDao.existBookAuthorByAuthorId(1)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteBookAuthorByBookIdWhenAuthorIdNotExist() {
        // Verificamos que no existe
        this.bookAuthorDao.existBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // Eliminamos
        this.bookAuthorDao.deleteBookAuthorByAuthorId(4)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findBookWithTheirAuthorsByBookId() {
        this.bookAuthorDao.findBookWithTheirAuthorsByBookId(1)
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
    void findBookWithTheirAuthorsByBookIdWhenIdNotExists() {
        this.bookAuthorDao.findBookWithTheirAuthorsByBookId(3)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByQuery() {
        var criteria = new BookCriteria("ri", null);
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("El zorro de arriba y el zorro de abajo", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("2002-05-06"), bookProjection.publicationDate());
                    Assertions.assertFalse(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("La ciudad y los perros", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByPublicationDate() {
        var criteria = new BookCriteria("", LocalDate.parse("1988-07-15"));
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Redoble por Rancas", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1988-07-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsFilteredByQueryAndPublicationDate() {
        var criteria = new BookCriteria("ciu", LocalDate.parse("1985-03-18"));
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("La ciudad y los perros", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1985-03-18"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookProjectionsWithoutFilter() {
        var criteria = new BookCriteria(" ", null);
        Pageable pageable = PageRequest.of(0, 5);
        this.bookAuthorDao.findAllToPage(criteria, pageable)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("El zorro de arriba y el zorro de abajo", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("2002-05-06"), bookProjection.publicationDate());
                    Assertions.assertFalse(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .expectNextCount(1)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals(2, bookProjection.authorNames().size());
                })
                .expectNextCount(1)
                .verifyComplete();
    }

}
