package com.web_tutorial.javabackend.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.web_tutorial.javabackend.config.RateLimitProperties;

class RateLimitServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-07-30T00:00:00Z"));
    private final RateLimitProperties.Policy policy =
            new RateLimitProperties.Policy(2, Duration.ofMinutes(1));
    private final RateLimitService service =
            new RateLimitService(properties(), clock);

    @Test
    void rejectsRequestsPastCapacityAndProvidesRetryMetadata() {
        assertThat(service.consume("login", "key", policy).allowed()).isTrue();
        assertThat(service.consume("login", "key", policy).remaining()).isZero();

        RateLimitDecision rejected = service.consume("login", "key", policy);

        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.limit()).isEqualTo(2);
        assertThat(rejected.remaining()).isZero();
        assertThat(rejected.retryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void separatesKeysAndResetsExpiredWindows() {
        service.consume("login", "first", policy);
        service.consume("login", "first", policy);

        assertThat(service.consume("login", "second", policy).allowed()).isTrue();
        assertThat(service.inspect("login", "first", policy).allowed()).isFalse();

        clock.advance(Duration.ofMinutes(1));

        assertThat(service.consume("login", "first", policy).allowed()).isTrue();
    }

    @Test
    void explicitResetClearsAccountFailureState() {
        service.consume("login-account", "account", policy);
        service.consume("login-account", "account", policy);

        service.reset("login-account", "account");

        assertThat(service.inspect("login-account", "account", policy).allowed()).isTrue();
    }

    @Test
    void rejectsNonPositiveDurationsAtConfigurationBoundary() {
        assertThatThrownBy(() -> new RateLimitProperties.Policy(1, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RateLimitProperties(
                true, 100, Duration.ofSeconds(-1), false, 1,
                policy, policy, policy, policy, policy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private RateLimitProperties properties() {
        return new RateLimitProperties(
                true, 100, Duration.ofMinutes(5), false, 1,
                policy, policy, policy, policy, policy);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
