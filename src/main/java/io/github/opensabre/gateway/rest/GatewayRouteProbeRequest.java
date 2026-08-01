package io.github.opensabre.gateway.rest;

import java.util.List;

/** 控制面提交的本次发布修订号和预期托管 Route ID。 */
public record GatewayRouteProbeRequest(String revision, List<String> routeIds) {
}
