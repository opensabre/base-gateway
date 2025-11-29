package io.github.opensabre.gateway.entity;

/**
 * @param pattern   请求url pattern
 * @param method    请求方法
 * @param authority 权限值
 */
public record Authority(String pattern, String method, String authority, String role) {
}