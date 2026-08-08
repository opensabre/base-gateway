package io.github.opensabre.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpCidrMatcherTest {
    @Test void matchesIpv4AndIpv6Networks() {
        assertThat(IpCidrMatcher.parse("10.0.0.0/8").matches("10.20.30.40")).isTrue();
        assertThat(IpCidrMatcher.parse("10.0.0.0/8").matches("11.0.0.1")).isFalse();
        assertThat(IpCidrMatcher.parse("2001:db8::/32").matches("2001:db8::42")).isTrue();
    }

    @Test void rejectsHostnamesAndInvalidPrefixes() {
        assertThatThrownBy(() -> IpCidrMatcher.parse("localhost")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IpCidrMatcher.parse("10.0.0.0/33")).isInstanceOf(IllegalArgumentException.class);
    }
}
