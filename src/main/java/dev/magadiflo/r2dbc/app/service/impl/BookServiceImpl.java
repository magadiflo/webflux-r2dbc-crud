package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterBookDTO;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import dev.magadiflo.r2dbc.app.persistence.repository.IBookRepository;
import dev.magadiflo.r2dbc.app.service.IBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements IBookService {

    private final IBookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Mono<Page<IBookProjection>> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        /*
        return bookRepository.findAllToPage(bookCriteria,pageable)
        		.collectList()
        		.switchIfEmpty(Mono.error(new ApiException("Not result", HttpStatus.NO_CONTENT)))
        		.zipWith(bookRepository.findCountBookAuthorByCriteria(bookCriteria))
        		.map(result -> new PageImpl<>(result.getT1(), pageable, result.getT2()));
         */
        return null;
    }

    @Override
    public Mono<IBookProjection> findBookById(Integer bookId) {
        //return bookRepository.findByBookId(bookId);
        return null;
    }

    @Override
    public Mono<Void> saveBook(RegisterBookDTO registerBookDTO) {
        /*
        Book book = null;

		try {
			book = modelMapper.map(registerBookDto, Book.class);
		} catch (Exception e) {
			log.error(e.getMessage());
			return Mono.error(new ApiException("Error in Detail", HttpStatus.NOT_FOUND));
		}

		return bookRepository.save(book).flatMap(bookEntity -> {
			List<BookAuthor> bookAuthors = registerBookDto.getAuthors().stream().map(authorId -> {

				return BookAuthor.builder()
						.authorId(authorId)
						.bookId(bookEntity.getBookId())
						.build();

			}).collect(Collectors.toList());
			return bookRepository.saveAllBookAuthor(bookAuthors).collectList().then();
		});
         */
        return null;
    }

    @Override
    public Mono<Void> deleteBook(Integer bookId) {
        /*
        return bookRepository.existBookAuthorByBookId(bookId)
		.flatMap(existBookAuthor -> {

			if(!existBookAuthor) {

				return bookRepository.deleteById(bookId);
			}

			return bookRepository.deleteBookAuthorByBookId(bookId)
					.then( bookRepository.deleteById(bookId));

		});
         */
        return null;
    }
}
