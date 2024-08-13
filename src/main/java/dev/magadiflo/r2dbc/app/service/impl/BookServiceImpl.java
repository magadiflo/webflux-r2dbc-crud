package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterBookDTO;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import dev.magadiflo.r2dbc.app.persistence.dao.IBookAuthorDao;
import dev.magadiflo.r2dbc.app.persistence.entity.Book;
import dev.magadiflo.r2dbc.app.persistence.entity.BookAuthor;
import dev.magadiflo.r2dbc.app.persistence.repository.IAuthorRepository;
import dev.magadiflo.r2dbc.app.persistence.repository.IBookRepository;
import dev.magadiflo.r2dbc.app.service.IBookService;
import dev.magadiflo.r2dbc.app.utils.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements IBookService {

    private final IBookRepository bookRepository;
    private final IAuthorRepository authorRepository;
    private final IBookAuthorDao bookAuthorDao;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<IBookProjection>> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        Mono<Long> countBookAuthorByCriteria = this.bookAuthorDao.findCountBookAuthorByCriteria(bookCriteria);
        return this.bookAuthorDao.findAllToPage(bookCriteria, pageable)
                .collectList()
                .switchIfEmpty(Mono.error(new ApiException("Not result", HttpStatus.NO_CONTENT)))
                .zipWith(countBookAuthorByCriteria, (iBookProjections, total) -> new PageImpl<>(iBookProjections, pageable, total));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<IBookProjection> findBookById(Integer bookId) {
        return this.bookAuthorDao.findAllBookAuthorByBookId(bookId)
                .switchIfEmpty(Mono.error(new ApiException("No hay resultados con bookId: %d".formatted(bookId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public Mono<Integer> saveBook(RegisterBookDTO registerBookDTO) {
        Mono<Book> bookMonoToSave = this.bookMapper.toBook(registerBookDTO);
        Mono<Book> bookMonoDB = bookMonoToSave.flatMap(this.bookRepository::save);

        return this.authorRepository.findAllAuthorsByIdIn(registerBookDTO.getAuthors())
                .collectList()
                .flatMap(authors -> {
                    if (authors.size() != registerBookDTO.getAuthors().size()) {
                        return Mono.error(new ApiException("Algunos autores no existen en la BD", HttpStatus.BAD_REQUEST));
                    }
                    return Mono.just(authors);
                }).zipWith(bookMonoDB, (authors, bookDB) -> {
                    List<BookAuthor> bookAuthorList = this.bookMapper.toBookAuthorList(authors, bookDB.getId());
                    return this.bookAuthorDao.saveAllBookAuthor(bookAuthorList).then(Mono.just(bookDB.getId()));
                })
                .flatMap(bookIdMono -> bookIdMono);
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteBook(Integer bookId) {
        return this.bookRepository.findById(bookId)
                .flatMap(bookDB -> this.bookAuthorDao.existBookAuthorByBookId(bookId))
                .flatMap(existsBookAuthor -> {
                    log.info("Existe el libro en la tabla book_authors?: {}", existsBookAuthor);
                    if (existsBookAuthor) {
                        return this.bookAuthorDao.deleteBookAuthorByBookId(bookId).then(Mono.just(true));
                    }
                    return Mono.just(true);
                })
                .flatMap(canContinue -> this.bookRepository.deleteById(bookId).then(Mono.just(true)))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el libro con id %s para eliminar".formatted(bookId), HttpStatus.NOT_FOUND)));
    }
}
