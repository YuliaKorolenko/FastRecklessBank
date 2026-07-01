package org.example.fastrecklessbank.api.dto;

/** Uniform error body returned by the API. */
public record ErrorResponse(
        String code,
        String message) {
}
