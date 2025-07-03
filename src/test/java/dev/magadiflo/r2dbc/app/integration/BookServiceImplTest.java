package dev.magadiflo.r2dbc.app.integration;

import dev.magadiflo.r2dbc.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.LocalDate;

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
    }

    @Test
    void getAllBookAuthorsToPage() {
    }

    @Test
    void saveBook() {
    }

    @Test
    void updateBook() {
    }

    @Test
    void deleteBook() {
    }
}