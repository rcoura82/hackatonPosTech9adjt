package br.gov.sus.rndsressarcimento.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String role
) {
}
