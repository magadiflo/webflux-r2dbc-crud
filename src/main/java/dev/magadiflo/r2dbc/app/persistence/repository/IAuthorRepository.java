package dev.magadiflo.r2dbc.app.persistence.repository;

import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAuthorRepository extends ReactiveCrudRepository<Author, Integer> {

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            INSERT INTO authors(first_name, last_name, birthdate)
            VALUES(:#{#author.firstName}, :#{#author.lastName}, :#{#author.birthdate})
            """)
    Mono<Integer> saveAuthor(@Param(value = "author") Author author);

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            UPDATE authors
            SET first_name = :#{#author.firstName},
                last_name = :#{#author.lastName},
                birthdate = :#{#author.birthdate}
            WHERE id = :#{#author.id}
            """)
    Mono<Integer> updateAuthor(@Param(value = "author") Author author);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, CONCAT(a.first_name, ' ', a.last_name) AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.id = :authorId
            """)
    Mono<IAuthorProjection> findByAuthorId(@Param(value = "authorId") Integer authorId);

    @Query(value = """
            SELECT a.id,
                    a.first_name,
                    a.last_name,
                    a.birthdate
            FROM authors AS a
            WHERE a.id IN(:authorIds)
            """)
    Flux<Author> findAllAuthorsByIdIn(List<Integer> authorIds);

    @Query(value = """
            SELECT COUNT(a.id)
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            """)
    Mono<Integer> findCountByQuery(String query);

    @Query(value = """
            SELECT  a.id,
                    a.first_name,
                    a.last_name,
                    a.birthdate
            FROM authors AS a
            WHERE a.first_name LIKE :#{'%' + #query + '%'}
                OR a.last_name LIKE :#{'%' + #query + '%'}
            ORDER BY a.id ASC
            LIMIT :#{#pageable.getPageSize()}
            OFFSET :#{#pageable.getOffset()}
            """)
    Flux<IAuthorProjection> findByQuery(String query, Pageable pageable);
}
