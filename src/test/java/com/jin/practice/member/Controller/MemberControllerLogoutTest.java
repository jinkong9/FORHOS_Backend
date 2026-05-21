package com.jin.practice.member.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberControllerLogoutTest {

    @Test
    void logoutExpiresFrontendAuthCookies() {
        MemberController controller = new MemberController(null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.logoutMember(response);

        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookieHeaders)
                .anySatisfy(header -> assertThat(header).startsWith("access_token=").contains("Max-Age=0"))
                .anySatisfy(header -> assertThat(header).startsWith("refresh_token=").contains("Max-Age=0"))
                .anySatisfy(header -> assertThat(header).startsWith("grant_type=").contains("Max-Age=0"));

        assertThat(setCookieHeaders).noneSatisfy(header -> assertThat(header).startsWith("accessToken="));
    }
}
