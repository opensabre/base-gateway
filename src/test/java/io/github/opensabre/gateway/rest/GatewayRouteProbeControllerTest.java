package io.github.opensabre.gateway.rest;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 内部探测只检查本实例运行时 Route ID，不调用下游 URI。 */
class GatewayRouteProbeControllerTest {

    @Test
    void shouldReturnMissingRouteIdsForLoadedRevision() {
        RouteLocator locator = () -> Flux.just(route("api-1"), route("unmanaged"));
        GatewayRouteProbeController controller = new GatewayRouteProbeController(locator);
        ReflectionTestUtils.setField(controller, "revision", "release-11");

        GatewayRouteProbeResponse response = controller.probe(
                new GatewayRouteProbeRequest("release-11", List.of("api-1", "api-2"))).block();

        assertThat(response.loaded()).containsExactly("api-1");
        assertThat(response.missing()).containsExactly("api-2");
    }

    private Route route(String id) {
        return Route.async().id(id).uri(URI.create("http://localhost"))
                .predicate(exchange -> true).build();
    }
}
