package dev.magadiflo.r2dbc.app.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookAuthorUpdateRequest(List<@NotNull Integer> authorIds) {
}
