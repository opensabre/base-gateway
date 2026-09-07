package io.github.opensabre.gateway.service.impl;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.gateway.entity.Authority;
import io.github.opensabre.gateway.provider.AuthorityMetaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import io.github.opensabre.gateway.service.IAuthorityMetaService;
import reactor.core.publisher.Flux;

/**
 * Created by zhoutaoo on 2018/5/27.
 */
@Slf4j
@Service
public class AuthorityMetaService implements IAuthorityMetaService {

    private final AuthorityMetaProvider authorityMetaProvider;

    public AuthorityMetaService(AuthorityMetaProvider authorityMetaProvider) {
        this.authorityMetaProvider = authorityMetaProvider;
    }

    @Cacheable(value = "gateway:authorities:role", key = "#p0")
    @Override
    public Flux<Authority> getAuthorityForRole(String role) {
        // 远程调用获取角色所拥有的权限资源
        return authorityMetaProvider.getAuthorityForRole(role)
                .filter(Result::isSuccess)
                .map(Result::getData)
                .flatMapMany(Flux::fromIterable)
                .map(resource -> new Authority(resource.getUrl(), resource.getMethod(), resource.getCode(), role))
                .onErrorResume(throwable -> {
                    log.error("Failed to get authorities for role: {}", role, throwable);
                    return Flux.empty();
                });
    }
}
