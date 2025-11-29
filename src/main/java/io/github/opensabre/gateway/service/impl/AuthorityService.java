package io.github.opensabre.gateway.service.impl;

import io.github.opensabre.gateway.entity.Authority;
import io.github.opensabre.gateway.service.IAuthorityMetaService;
import io.github.opensabre.gateway.service.IAuthorityService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Set;

/**
 * Created by zhoutaoo on 2018/5/27.
 */
@Slf4j
@Service
public class AuthorityService implements IAuthorityService {

    private final IAuthorityMetaService authorityMetaService;

    public AuthorityService(IAuthorityMetaService authorityMetaService) {
        this.authorityMetaService = authorityMetaService;
    }

    @Override
    public Flux<Authority> getAuthorityForRoles(String... roles) {
        return getAuthorityForRoles(Set.of(roles));
    }

    @Override
    public Flux<Authority> getAuthorityForRoles(@Nonnull Set<String> roles) {
        // 根据用户角色从远程加载用户所拥有的所有resource，多角色循环处理
        return Flux.fromIterable(roles)
                .flatMap(authorityMetaService::getAuthorityForRole);
    }
}