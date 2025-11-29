package io.github.opensabre.gateway.service;

import io.github.opensabre.gateway.entity.Authority;
import reactor.core.publisher.Flux;


/**
 * Created by zhoutaoo on 2025/11/27.
 */
public interface IAuthorityMetaService {

    /**
     * 根据角色code查询出该角色所拥有的权限资源
     *
     * @param role 角色code
     * @return Flux<Authority>
     */
    Flux<Authority> getAuthorityForRole(String role);
}