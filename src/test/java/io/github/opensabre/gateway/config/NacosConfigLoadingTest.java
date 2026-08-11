package io.github.opensabre.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

class NacosConfigLoadingTest {

    @Test
    void usesConfigDataWithoutLegacyBootstrapStarter() {
        assertThat(ClassUtils.isPresent(
                "org.springframework.cloud.bootstrap.marker.Marker",
                getClass().getClassLoader()))
                .as("legacy Bootstrap would load base-gateway.yml a second time and shadow refreshed values")
                .isFalse();
    }
}
