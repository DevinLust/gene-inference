package com.progressengine.geneinference.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;

@Configuration
@Profile("!ci")
public class JwtValidationConfig {

    @Bean
    public OAuth2TokenValidator<Jwt> jwtValidator(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer
    ) {
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> withAudience = token -> {
            List<String> aud = token.getAudience();
            return (aud != null && aud.contains("authenticated"))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Missing required audience 'authenticated'", null)
            );
        };

        return new DelegatingOAuth2TokenValidator<>(withIssuer, withTimestamp, withAudience);
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            OAuth2TokenValidator<Jwt> jwtValidator
    ) {
        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        if (decoder instanceof NimbusJwtDecoder nimbus) {
            nimbus.setJwtValidator(jwtValidator);
        }
        return decoder;
    }
}