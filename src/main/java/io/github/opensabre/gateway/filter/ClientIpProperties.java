package io.github.opensabre.gateway.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** Defines proxies allowed to supply X-Forwarded-For. */
@ConfigurationProperties(prefix = "opensabre.gateway.client-ip")
public class ClientIpProperties {
    private List<String> trustedProxies = new ArrayList<>();

    public List<String> getTrustedProxies() { return trustedProxies; }
    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies == null ? new ArrayList<>() : new ArrayList<>(trustedProxies);
    }
}
