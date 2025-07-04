package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookUpdateRequest;
import dev.magadiflo.r2dbc.app.exception.AuthorIdsNotFoundException;
import dev.magadiflo.r2dbc.app.exception.BookNotFoundException;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import dev.magadiflo.r2dbc.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@SpringBootTest
class BookServiceImplTest extends AbstractTest {

    @Autowired
    private BookService bookService;

    @Test
    void findAllBooks() {
        this.bookService.findAllBooks()
                .doOnNext(bookResponse -> log.info("{}", bookResponse))
                .as(StepVerifier::create)
                .assertNext(bookResponse -> {
                    Assertions.assertEquals(1, bookResponse.id());
                    Assertions.assertEquals("Los ríos profundos", bookResponse.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookResponse.publicationDate());
                    Assertions.assertTrue(bookResponse.onlineAvailability());
                })
                .assertNext(bookResponse -> Assertions.assertTrue(bookResponse.onlineAvailability()))
                .assertNext(bookResponse -> Assertions.assertFalse(bookResponse.onlineAvailability()))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findBookById() {
        this.bookService.findBookById(1)
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Los ríos profundos", bookProjection.title());
                    Assertions.assertEquals(LocalDate.parse("1999-01-15"), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertFalse(bookProjection.authorNames().isEmpty());
                    Assertions.assertEquals(2, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }

    @Test
    void givenNonExistingBookId_whenDeleting_thenThrowsError() {
        this.bookService.findBookById(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void findBooksWithAuthorsByCriteria_withData() {
        String query = "ri";
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<BookProjection>> result = this.bookService.findBooksWithAuthorsByCriteria(query, null, pageNumber, pageSize)
                .doOnNext(bookProjections -> log.info("{}", bookProjections.getContent()));

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertNotNull(page);
                    Assertions.assertEquals(2, page.getContent().size());
                    Assertions.assertEquals(0, page.getNumber());
                    Assertions.assertEquals(2, page.getSize());
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertTrue(page.isFirst());
                    Assertions.assertTrue(page.isLast());

                    BookProjection firstBookProjection = page.getContent().get(0);
                    Assertions.assertNotNull(firstBookProjection.title());
                    Assertions.assertNotNull(firstBookProjection.publicationDate());
                    Assertions.assertFalse(firstBookProjection.onlineAvailability());
                    Assertions.assertNull(firstBookProjection.authors());
                    Assertions.assertTrue(firstBookProjection.authorNames().isEmpty());

                    BookProjection secondBookProjection = page.getContent().get(1);
                    Assertions.assertNotNull(secondBookProjection.title());
                    Assertions.assertNotNull(secondBookProjection.publicationDate());
                    Assertions.assertTrue(secondBookProjection.onlineAvailability());
                    Assertions.assertNotNull(secondBookProjection.authors());
                    Assertions.assertFalse(secondBookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void findBooksWithAuthorsByCriteria_withoutData() {
        int pageNumber = 0;
        int pageSize = 2;

        Mono<Page<BookProjection>> result = this.bookService.findBooksWithAuthorsByCriteria(" ", LocalDate.now(), pageNumber, pageSize);

        StepVerifier.create(result)
                .assertNext(page -> {
                    Assertions.assertTrue(page.getContent().isEmpty());
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.isFirst());
                    Assertions.assertTrue(page.isLast());
                })
                .verifyComplete();
    }

    @Test
    void givenValidBookRequestWithAuthors_whenSave_thenReturnBookProjection() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, List.of(2, 3, 4)))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertEquals(3, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }

    @Test
    void givenBookRequestWithoutAuthors_whenSave_thenReturnBookProjection() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, null))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenBookRequestWithNonExistingAuthor_whenSave_thenThrowError() {
        this.bookService.saveBook(new BookRequest("Spring WebFlux", LocalDate.now(), true, List.of(2, 4, 10)))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorIdsNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("Algunos IDs de autores no existen en el sistema", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenExistingBook_whenUpdate_thenReturnUpdatedProjection() {
        this.bookService.updateBook(3, new BookUpdateRequest("Kafka", LocalDate.now(), true))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertEquals("Kafka", bookProjection.title());
                    Assertions.assertEquals(LocalDate.now(), bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenNonExistingBookId_whenUpdate_thenThrowError() {
        this.bookService.updateBook(5, new BookUpdateRequest("Kafka", LocalDate.now(), true))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenNonExistingBookId_whenUpdateAuthors_thenThrowError() {
        this.bookService.updateBookAuthors(5, List.of())
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void givenBookIdAndEmptyAuthors_whenUpdateAuthors_thenRemoveAllRelations() {
        this.bookService.updateBookAuthors(1, List.of())
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNull(bookProjection.authors());
                    Assertions.assertTrue(bookProjection.authorNames().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void givenBookIdAndValidAuthors_whenUpdateAuthors_thenUpdateRelations() {
        this.bookService.updateBookAuthors(1, List.of(3, 4, 2))
                .doOnNext(bookProjection -> log.info("{}", bookProjection))
                .as(StepVerifier::create)
                .assertNext(bookProjection -> {
                    Assertions.assertNotNull(bookProjection.title());
                    Assertions.assertNotNull(bookProjection.publicationDate());
                    Assertions.assertTrue(bookProjection.onlineAvailability());
                    Assertions.assertNotNull(bookProjection.authors());
                    Assertions.assertEquals(3, bookProjection.authorNames().size());
                })
                .verifyComplete();
    }


    @Test
    void givenBookIdAndNonExistingAuthors_whenUpdateAuthors_thenThrowError() {
        this.bookService.updateBookAuthors(1, List.of(3, 4, 2, 5))
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(AuthorIdsNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("Algunos IDs de autores no existen en el sistema", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void deleteBookWithoutAssociatedAuthors() {
        this.bookService.deleteBook(3)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteBookWithAssociatedBooks() {
        this.bookService.deleteBook(1)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void throwErrorWhenDeleteBookWithIdThatDoesNotExist() {
        this.bookService.deleteBook(5)
                .as(StepVerifier::create)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertEquals(BookNotFoundException.class, throwable.getClass());
                    Assertions.assertEquals("El libro [id=5] no fue encontrado", throwable.getMessage());
                })
                .verify();
    }
}
