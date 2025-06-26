package dev.magadiflo.r2dbc.app.repository;

import dev.magadiflo.r2dbc.app.entity.Book;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface BookRepository extends ReactiveCrudRepository<Book, Integer> {
}
