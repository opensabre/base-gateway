package io.github.opensabre.gateway.actuator;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/** Actuator management endpoint exposing only allow-listed effective gateway runtime settings. */
@Component
@Endpoint(id = "gatewayruntime")
public class GatewayRuntimeEndpoint {

    private final Environment environment;
    private final RouteLocator routeLocator;
    private final BuildProperties buildProperties;
    private final String revision;

    public GatewayRuntimeEndpoint(Environment environment, RouteLocator routeLocator,
            BuildProperties buildProperties,
            @Value("${opensabre.gateway.revision:UNMANAGED}") String revision) {
        this.environment = environment;
        this.routeLocator = routeLocator;
        this.buildProperties = buildProperties;
        this.revision = revision;
    }

    @ReadOperation
    public Mono<GatewayRuntimeSnapshot> snapshot() {
        return routeLocator.getRoutes().count().map(count -> createSnapshot(Math.toIntExact(count)));
    }

    GatewayRuntimeSnapshot createSnapshot(int routeCount) {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        int processors = Runtime.getRuntime().availableProcessors();
        Map<String, String> sources = new LinkedHashMap<>();
        int workerThreads = integer("reactor.netty.ioWorkerCount", Math.max(processors, 4), sources);
        int selectorThreads = integer("reactor.netty.ioSelectCount", -1, sources);
        String poolType = text("spring.cloud.gateway.httpclient.pool.type", "ELASTIC", sources);
        int maxConnections = integer("spring.cloud.gateway.httpclient.pool.max-connections", 500, sources);
        long acquireTimeout = duration("spring.cloud.gateway.httpclient.pool.acquire-timeout", 45_000, sources);
        long connectTimeout = integer("spring.cloud.gateway.httpclient.connect-timeout", 30_000, sources);
        Long responseTimeout = optionalDuration("spring.cloud.gateway.httpclient.response-timeout", sources);
        Long maxIdleTime = optionalDuration("spring.cloud.gateway.httpclient.pool.max-idle-time", sources);
        Long maxLifeTime = optionalDuration("spring.cloud.gateway.httpclient.pool.max-life-time", sources);

        return new GatewayRuntimeSnapshot(revision, buildProperties.getVersion(),
                ManagementFactory.getRuntimeMXBean().getUptime() / 1000, processors,
                new GatewayRuntimeSnapshot.JvmSnapshot(System.getProperty("java.version"),
                        System.getProperty("java.vendor"), memory.getHeapMemoryUsage().getUsed(),
                        memory.getHeapMemoryUsage().getMax(), memory.getNonHeapMemoryUsage().getUsed(),
                        threads.getThreadCount(), threads.getPeakThreadCount()),
                new GatewayRuntimeSnapshot.NettySnapshot(workerThreads, selectorThreads),
                new GatewayRuntimeSnapshot.HttpClientSnapshot(poolType, maxConnections,
                        acquireTimeout, connectTimeout, responseTimeout, maxIdleTime, maxLifeTime),
                routeCount, Map.copyOf(sources));
    }

    private String text(String key, String defaultValue, Map<String, String> sources) {
        String value = environment.getProperty(key);
        sources.put(key, value == null ? "DEFAULT" : "CONFIGURED");
        return value == null ? defaultValue : value;
    }

    private int integer(String key, int defaultValue, Map<String, String> sources) {
        String systemValue = System.getProperty(key);
        Integer configured = environment.getProperty(key, Integer.class);
        sources.put(key, systemValue != null ? "SYSTEM_PROPERTY" : configured == null ? "DEFAULT" : "CONFIGURED");
        return systemValue != null ? Integer.parseInt(systemValue) : configured == null ? defaultValue : configured;
    }

    private long duration(String key, long defaultValue, Map<String, String> sources) {
        Long value = optionalDuration(key, sources);
        return value == null ? defaultValue : value;
    }

    private Long optionalDuration(String key, Map<String, String> sources) {
        String value = environment.getProperty(key);
        sources.put(key, value == null ? "DEFAULT" : "CONFIGURED");
        if (value == null) return null;
        try {
            return Duration.parse(value).toMillis();
        } catch (RuntimeException ignored) {
            return Long.parseLong(value);
        }
    }
}
