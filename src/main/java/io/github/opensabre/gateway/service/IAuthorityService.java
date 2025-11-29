package io.github.opensabre.gateway.service;

import io.github.opensabre.gateway.entity.Authority;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * Created by zhoutaoo on 2025/11/27.
 */
public interface IAuthorityService {
    /**
     * 获取角色列表中对应的authorities的集合
     *
     * @param roles 角色数组
     * @return Set<Authority>
     */
    Flux<Authority> getAuthorityForRoles(String... roles);

    /**
     * 获取角色列表中对应的authorities的集合
     *
     * @param roles 角色集合
     * @return Set<Authority>
     */
    Flux<Authority> getAuthorityForRoles(Set<String> roles);
}