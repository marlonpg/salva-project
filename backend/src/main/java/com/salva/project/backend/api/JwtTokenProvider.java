package com.salva.project.backend.api;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	public String generateToken(String email, Instant expiresAt) {
		return Jwts.builder()
			.claim("email", email)
			.issuedAt(new Date())
			.expiration(new Date(expiresAt.toEpochMilli()))
			.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), Jwts.SIG.HS256)
			.compact();
	}

	public Map<String, Object> parseToken(String token) throws JwtException {
		var claims = Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return new HashMap<>(claims);
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public String extractEmail(String token) {
		try {
			Map<String, Object> claims = parseToken(token);
			return (String) claims.get("email");
		} catch (Exception e) {
			return null;
		}
	}
}
