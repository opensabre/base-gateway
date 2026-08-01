package io.github.opensabre.gateway.rest;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** 实例修订号端点只返回当前环境实际加载的值。 */
class GatewayRevisionControllerTest {

    @Test
    void shouldReturnLoadedRevision() {
        GatewayRevisionController controller = new GatewayRevisionController();
        ReflectionTestUtils.setField(controller, "revision", "release-42");

        assertThat(controller.revision()).containsEntry("revision", "release-42");
    }
}
