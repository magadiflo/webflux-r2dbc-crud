package dev.magadiflo.r2dbc.app.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "book_authors")
public class BookAuthor {
    private Integer bookId;
    private Integer authorId;
}
