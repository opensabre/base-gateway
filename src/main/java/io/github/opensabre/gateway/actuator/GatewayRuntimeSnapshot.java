package io.github.opensabre.gateway.actuator;

import java.util.Map;

/** Safe, read-only snapshot of the effective runtime and capacity settings. */
public record GatewayRuntimeSnapshot(
        String revision,
        String applicationVersion,
        long uptimeSeconds,
        int availableProcessors,
        JvmSnapshot jvm,
        NettySnapshot netty,
        HttpClientSnapshot httpClient,
        int routeCount,
        Map<String, String> sources) {

    public record JvmSnapshot(String javaVersion, String vendor, long heapUsedBytes,
            long heapMaxBytes, long nonHeapUsedBytes, int liveThreads, int peakThreads) {}

    public record NettySnapshot(int workerThreads, int selectorThreads) {}

    public record HttpClientSnapshot(String poolType, int maxConnections,
            long acquireTimeoutMillis, long connectTimeoutMillis,
            Long responseTimeoutMillis, Long maxIdleTimeMillis, Long maxLifeTimeMillis) {}
}
