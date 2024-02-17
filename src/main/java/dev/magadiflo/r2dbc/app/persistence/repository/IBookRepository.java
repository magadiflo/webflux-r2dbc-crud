package dev.magadiflo.r2dbc.app.persistence.repository;

import dev.magadiflo.r2dbc.app.persistence.entity.Book;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IBookRepository extends ReactiveCrudRepository<Book, Integer> {
}
