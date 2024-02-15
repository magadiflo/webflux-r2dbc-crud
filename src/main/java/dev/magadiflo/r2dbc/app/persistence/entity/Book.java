package dev.magadiflo.r2dbc.app.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "books")
public class Book {
    @Id
    private Integer id;
    private String title;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate publicationDate;
    private Boolean onlineAvailability;
}
