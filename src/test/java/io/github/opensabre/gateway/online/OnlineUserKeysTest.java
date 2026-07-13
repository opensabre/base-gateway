package io.github.opensabre.gateway.online;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnlineUserKeysTest {

    @Test
    void shouldBuildOnlineUserKeysFromSessionNamespace() {
        assertThat(OnlineUserKeys.onlineSessionsKey("opensabre:gateway:session"))
                .isEqualTo("opensabre:gateway:session:online:sessions");
        assertThat(OnlineUserKeys.onlineSessionKey("opensabre:gateway:session", "s1"))
                .isEqualTo("opensabre:gateway:session:online:session:s1");
    }

    @Test
    void shouldUseDefaultSessionNamespaceWhenBlank() {
        assertThat(OnlineUserKeys.onlineSessionsKey(""))
                .isEqualTo("opensabre:gateway:session:online:sessions");
    }
}
