package io.github.opensabre.gateway.filter;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** IP/CIDR matcher supporting IPv4 and IPv6 without DNS lookups. */
final class IpCidrMatcher {

    private final byte[] network;
    private final int prefixLength;

    private IpCidrMatcher(byte[] network, int prefixLength) {
        this.network = network;
        this.prefixLength = prefixLength;
    }

    static IpCidrMatcher parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IP/CIDR 不能为空");
        }
        String normalized = value.trim();
        String[] parts = normalized.split("/", -1);
        if (parts.length > 2 || parts[0].isBlank()) {
            throw new IllegalArgumentException("非法 IP/CIDR：" + value);
        }
        byte[] address = address(parts[0]);
        int maxBits = address.length * Byte.SIZE;
        int prefix = parts.length == 1 ? maxBits : parsePrefix(parts[1], maxBits, value);
        return new IpCidrMatcher(mask(address, prefix), prefix);
    }

    boolean matches(String candidate) {
        if (candidate == null || candidate.isBlank()) return false;
        byte[] address;
        try {
            address = address(candidate.trim());
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return address.length == network.length && java.util.Arrays.equals(mask(address, prefixLength), network);
    }

    private static byte[] address(String value) {
        if (!value.matches("[0-9a-fA-F:.]+")) {
            throw new IllegalArgumentException("非法 IP 地址：" + value);
        }
        try {
            return InetAddress.getByName(value).getAddress();
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("非法 IP 地址：" + value, exception);
        }
    }

    private static int parsePrefix(String value, int maxBits, String source) {
        try {
            int prefix = Integer.parseInt(value);
            if (prefix < 0 || prefix > maxBits) throw new NumberFormatException();
            return prefix;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("非法 CIDR 前缀：" + source, exception);
        }
    }

    private static byte[] mask(byte[] address, int prefix) {
        byte[] result = address.clone();
        int fullBytes = prefix / Byte.SIZE;
        int remainingBits = prefix % Byte.SIZE;
        if (remainingBits > 0) {
            result[fullBytes] &= (byte) (0xff << (Byte.SIZE - remainingBits));
            fullBytes++;
        }
        java.util.Arrays.fill(result, fullBytes, result.length, (byte) 0);
        return result;
    }
}
