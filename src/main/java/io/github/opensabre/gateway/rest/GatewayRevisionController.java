package io.github.opensabre.gateway.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 暴露当前实例实际加载的控制面发布修订号，供发布后生效确认使用。 */
@RefreshScope
@RestController
public class GatewayRevisionController {

    @Value("${opensabre.gateway.revision:UNMANAGED}")
    private String revision;

    /** 返回当前内存环境中的修订号，不读取 Nacos 最新值。 */
    @GetMapping("/internal/gateway/revision")
    public Map<String, String> revision() {
        return Map.of("revision", revision);
    }
}
