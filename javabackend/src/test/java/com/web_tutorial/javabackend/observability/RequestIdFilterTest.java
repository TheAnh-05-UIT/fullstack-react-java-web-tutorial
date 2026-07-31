package com.web_tutorial.javabackend.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void validClientRequestIdIsPreserved() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER_NAME, "safe-request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (currentRequest, currentResponse) ->
                assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isEqualTo("safe-request_123"));

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isEqualTo("safe-request_123");
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void unsafeClientRequestIdIsReplaced() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER_NAME, "injected\r\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (currentRequest, currentResponse) -> {
        });

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME))
                .matches("[0-9a-f-]{36}")
                .doesNotContain("\r", "\n");

        MockHttpServletRequest oversizedRequest = new MockHttpServletRequest();
        oversizedRequest.addHeader(RequestIdFilter.HEADER_NAME, "a".repeat(65));
        MockHttpServletResponse oversizedResponse = new MockHttpServletResponse();
        filter.doFilter(oversizedRequest, oversizedResponse, (currentRequest, currentResponse) -> {
        });
        assertThat(oversizedResponse.getHeader(RequestIdFilter.HEADER_NAME))
                .matches("[0-9a-f-]{36}");
    }

    @Test
    void mdcIsCleanedWhenDownstreamFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response,
                (currentRequest, currentResponse) -> {
                    throw new IllegalStateException("test failure");
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).matches("[0-9a-f-]{36}");
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
