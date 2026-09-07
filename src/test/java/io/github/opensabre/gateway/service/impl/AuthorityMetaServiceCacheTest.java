package io.github.opensabre.gateway.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorityMetaServiceCacheTest {

    @Test
    void cacheKeyMustNotDependOnCompilerParameterNames() throws Exception {
        Cacheable cacheable = AuthorityMetaService.class
                .getMethod("getAuthorityForRole", String.class)
                .getAnnotation(Cacheable.class);

        assertThat(cacheable.key()).isEqualTo("#p0");
    }
}
