package io.github.opensabre.gateway.online;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OnlineUserRecordServiceTest {

    @Test
    void shouldSkipAnonymousUser() {
        var anonymous = new TestingAuthenticationToken("anonymousUser", "n/a");
        anonymous.setAuthenticated(true);

        assertThat(OnlineUserRecordService.shouldRecord(anonymous)).isFalse();
    }

    @Test
    void shouldRecordAuthenticatedUser() {
        var authentication = new TestingAuthenticationToken("admin", "n/a");
        authentication.setAuthenticated(true);

        assertThat(OnlineUserRecordService.shouldRecord(authentication)).isTrue();
    }

    @Test
    void shouldUseLoginUsernameFromOAuth2SubjectWhenAuthenticationNameIsDisplayName() {
        var principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "admin", "name", "管理员"),
                "name");
        var authentication = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "opensabre");

        assertThat(authentication.getName()).isEqualTo("管理员");
        assertThat(OnlineUserRecordService.resolveUsername(authentication)).isEqualTo("admin");
    }

}
