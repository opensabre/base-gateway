package io.github.opensabre.gateway.rest;

import java.util.List;

/** 单个网关实例对预期 Route ID 的装载检查结果。 */
public record GatewayRouteProbeResponse(String revision, List<String> loaded, List<String> missing) {
}
