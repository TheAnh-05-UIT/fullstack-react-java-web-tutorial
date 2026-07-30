package com.web_tutorial.javabackend.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimitKeyFactoryTest {

    private final RateLimitKeyFactory factory = new RateLimitKeyFactory();

    @Test
    void normalizesAndHashesEmailWithoutRetainingRawIdentifier() {
        String key = factory.email("  User@Example.COM ");

        assertThat(key).isEqualTo(factory.email("user@example.com"));
        assertThat(key).hasSize(64);
        assertThat(key).doesNotContain("user", "@", "example");
    }

    @Test
    void canonicalizesEquivalentIpv6AddressesBeforeHashing() {
        assertThat(factory.ipAddress("2001:db8:0:0:0:0:0:1"))
                .isEqualTo(factory.ipAddress("2001:db8::1"));
    }

    @Test
    void producesDifferentKeysForDifferentAccounts() {
        assertThat(factory.email("first@example.com"))
                .isNotEqualTo(factory.email("second@example.com"));
    }
}
