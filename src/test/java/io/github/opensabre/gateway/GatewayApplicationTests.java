package io.github.opensabre.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.discovery.locator.enabled=false"
})
public class GatewayApplicationTests {

    @Test
    public void contextLoads() {

    }
}
