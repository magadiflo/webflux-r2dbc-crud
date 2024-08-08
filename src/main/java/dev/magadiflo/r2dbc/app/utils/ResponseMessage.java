package dev.magadiflo.r2dbc.app.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ResponseMessage<T> {
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T content;
}
