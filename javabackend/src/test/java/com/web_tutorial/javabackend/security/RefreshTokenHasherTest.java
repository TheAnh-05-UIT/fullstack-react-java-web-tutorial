package com.web_tutorial.javabackend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.web_tutorial.javabackend.service.security.RefreshTokenHasher;

class RefreshTokenHasherTest {

    private final RefreshTokenHasher hasher = new RefreshTokenHasher();

    @Test
    void hashingIsDeterministicLowercaseSha256Hex() {
        String first = hasher.hash("high-entropy-refresh-token");
        String second = hasher.hash("high-entropy-refresh-token");

        assertThat(first)
                .isEqualTo(second)
                .hasSize(64)
                .matches("[0-9a-f]{64}");
    }

    @Test
    void differentTokensProduceDifferentHashes() {
        assertThat(hasher.hash("refresh-token-one"))
                .isNotEqualTo(hasher.hash("refresh-token-two"));
    }
}
