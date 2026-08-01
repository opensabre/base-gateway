package io.github.opensabre.gateway.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 在不访问下游业务的情况下确认本实例已经构建预期的 Gateway Route。 */
@RefreshScope
@RestController
public class GatewayRouteProbeController {

    private final RouteLocator routeLocator;

    @Value("${opensabre.gateway.revision:UNMANAGED}")
    private String revision;

    public GatewayRouteProbeController(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    /** 修订号不一致时拒绝用旧实例路由表产生误导性探测结果。 */
    @PostMapping("/internal/gateway/routes/probe")
    public Mono<GatewayRouteProbeResponse> probe(@RequestBody GatewayRouteProbeRequest request) {
        if (request.revision() == null || !request.revision().equals(revision)) {
            return Mono.error(new IllegalStateException("实例尚未加载指定发布修订号"));
        }
        Set<String> expected = new LinkedHashSet<>(request.routeIds() == null ? List.of() : request.routeIds());
        return routeLocator.getRoutes().map(route -> route.getId()).filter(expected::contains).collectList()
                .map(loaded -> {
                    Set<String> missing = new LinkedHashSet<>(expected);
                    missing.removeAll(loaded);
                    return new GatewayRouteProbeResponse(revision, List.copyOf(loaded), List.copyOf(missing));
                });
    }
}
