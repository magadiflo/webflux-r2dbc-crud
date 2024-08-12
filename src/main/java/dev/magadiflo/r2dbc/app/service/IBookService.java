package dev.magadiflo.r2dbc.app.service;

import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterBookDTO;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface IBookService {
    Mono<Page<IBookProjection>> findAllToPage(BookCriteria bookCriteria, Pageable pageable);

    Mono<IBookProjection> findBookById(Integer bookId);

    Mono<Integer> saveBook(RegisterBookDTO registerBookDTO);

    Mono<Void> deleteBook(Integer bookId);
}
