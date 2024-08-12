package dev.magadiflo.r2dbc.app.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RegisterBookDTO {
    private String title;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate publicationDate;
    private List<Integer> authors;
    private Boolean onlineAvailability = false;
}
