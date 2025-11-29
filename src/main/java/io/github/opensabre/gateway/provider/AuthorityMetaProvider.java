package io.github.opensabre.gateway.provider;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.gateway.entity.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Created by zhoutaoo on 2025/11/26.
 */
@Component
@HttpExchange(value = "lb://base-organization")
public interface AuthorityMetaProvider {
    /**
     * 获取角色对应权限元数据
     *
     * @param roleCode 角色code
     * @return <pre>
     * Result:
     * {
     *   code:"000000"
     *   mesg:"请求成功"
     *   data: 返回数据
     * }
     * </pre>
     */
    @GetExchange("/resource/role/{roleCode}")
    Mono<Result<List<Resource>>> getAuthorityForRole(@PathVariable("roleCode") String roleCode);

    /**
     * Fallback 降级
     */
    class AuthProviderFallback implements AuthorityMetaProvider {

        @Override
        public Mono<Result<List<Resource>>> getAuthorityForRole(String roleCode) {
            return Mono.just(Result.fail());
        }
    }
}
