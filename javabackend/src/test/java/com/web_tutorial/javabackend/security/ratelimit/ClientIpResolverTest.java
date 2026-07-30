package com.web_tutorial.javabackend.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.web_tutorial.javabackend.config.RateLimitProperties;

class ClientIpResolverTest {

    @Test
    void ignoresSpoofedForwardedHeaderByDefault() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertThat(new ClientIpResolver(properties(false, 1)).resolve(request))
                .isEqualTo("192.0.2.10");
    }

    @Test
    void usesConfiguredTrustedHopFromRightWhenProxyModeIsEnabled() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "198.51.100.9, 203.0.113.10");

        assertThat(new ClientIpResolver(properties(true, 1)).resolve(request))
                .isEqualTo("203.0.113.10");
        assertThat(new ClientIpResolver(properties(true, 2)).resolve(request))
                .isEqualTo("198.51.100.9");
    }

    @Test
    void fallsBackToRemoteAddressForInvalidForwardedValue() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "attacker.example");

        assertThat(new ClientIpResolver(properties(true, 1)).resolve(request))
                .isEqualTo("192.0.2.10");
    }

    @Test
    void fallsBackWhenTrustedHopCountExceedsForwardedChain() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertThat(new ClientIpResolver(properties(true, 2)).resolve(request))
                .isEqualTo("192.0.2.10");
    }

    @Test
    void acceptsIpv6FromConfiguredTrustedHop() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "2001:db8::1");

        assertThat(new ClientIpResolver(properties(true, 1)).resolve(request))
                .isEqualTo("2001:db8::1");
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.10");
        return request;
    }

    private RateLimitProperties properties(boolean trustForwarded, int hops) {
        RateLimitProperties.Policy policy =
                new RateLimitProperties.Policy(2, Duration.ofMinutes(1));
        return new RateLimitProperties(
                true, 100, Duration.ofMinutes(5), trustForwarded, hops,
                policy, policy, policy, policy, policy);
    }
}
