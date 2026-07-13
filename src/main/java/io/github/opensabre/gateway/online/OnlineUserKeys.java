package io.github.opensabre.gateway.online;

public final class OnlineUserKeys {

    public static final String DEFAULT_SESSION_NAMESPACE = "opensabre:gateway:session";
    public static final String ONLINE_SESSIONS_SUFFIX = ":online:sessions";
    public static final String ONLINE_SESSION_SUFFIX = ":online:session:";

    private OnlineUserKeys() {
    }

    public static String onlineSessionsKey(String namespace) {
        return normalize(namespace) + ONLINE_SESSIONS_SUFFIX;
    }

    public static String onlineSessionKey(String namespace, String sessionId) {
        return normalize(namespace) + ONLINE_SESSION_SUFFIX + sessionId;
    }

    private static String normalize(String namespace) {
        return namespace == null || namespace.isBlank() ? DEFAULT_SESSION_NAMESPACE : namespace;
    }
}
