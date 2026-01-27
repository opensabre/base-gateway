# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Cloud Gateway-based API gateway application (`base-gateway`) that provides routing, authentication, rate limiting, and API documentation aggregation capabilities. It's part of the Opensabre microservices ecosystem.

## Development Commands

### Build and Run
- **Run locally**: `mvn spring-boot:run`
- **Build JAR**: `mvn clean package`
- **Run tests**: `mvn test`
- **Build Docker image**: `mvn docker:build` (legacy) or `mvn compile jib:build` (using Jib)

### Docker Build with Jib
The project uses Jib for containerization. Configure environment variables:
- `REGISTRY_URL`: Docker registry URL
- `secret.id`: Registry username
- `secret.key`: Registry password

Build command: `mvn compile jib:build`

### Prerequisites
- Redis (for rate limiting): `REDIS_HOST`, `REDIS_PORT` environment variables
- Nacos (for service discovery/config): `REGISTER_HOST`, `REGISTER_PORT` environment variables
- Sentinel dashboard (optional): `SENTINEL_DASHBOARD_HOST`, `SENTINEL_DASHBOARD_PORT`

## Architecture

### Key Components

#### 1. **Security Configuration** (`src/main/java/io/github/opensabre/gateway/config/`)
- `ResourceServerConfig.java`: OAuth2 resource server with JWT authentication
- `ClientServerConfig.java`: OAuth2 client configuration for authorization code flow
- `OpensabreGatewayConfig.java`: Loads gateway-specific configuration

#### 2. **Authorization Manager** (`src/main/java/io/github/opensabre/gateway/manager/`)
- `DynamicAuthorizationManager.java`: Reactive authorization manager for URL-level permissions

#### 3. **Gateway Filters** (`src/main/java/io/github/opensabre/gateway/filter/`)
- `AccessGatewayFilter.java`: Global filter for URL permission validation

#### 4. **Rate Limiting** (`src/main/java/io/github/opensabre/gateway/config/`)
- `RequestRateLimiterConfig.java`: Redis-based rate limiting with `apiKeyResolver` bean
- `DefaultRedisRateLimiter.java`: Custom Redis rate limiter implementation

#### 5. **Service Layer** (`src/main/java/io/github/opensabre/gateway/service/`)
- `IAuthorityService/AuthorityService.java`: Authority management
- `IAuthorityMetaService/AuthorityMetaService.java`: Authority metadata service

#### 6. **Metadata Provider** (`src/main/java/io/github/opensabre/gateway/provider/`)
- `AuthorityMetaProvider.java`: Authority metadata provider

#### 7. **Exception Handling** (`src/main/java/io/github/opensabre/gateway/exception/`)
- `CustomErrorWebExceptionHandler.java`: Global exception handler for gateway errors
- `GateWayExceptionHandlerAdvice.java`: Exception handling advice

#### 8. **Entities** (`src/main/java/io/github/opensabre/gateway/entity/`)
- `Authority.java`: Permission entity with pattern and method fields
- `Resource.java`: Resource entity

#### 9. **Other Configurations**
- `CacheConfig.java`: Caffeine cache configuration
- `HttpExchangeConfig.java`: HTTP exchange configuration

### Configuration Files

#### 1. **bootstrap.yml**
- Server port: 8443
- Application name: `base-gateway`
- Nacos service discovery and configuration
- Sentinel dashboard integration

#### 2. **application.yml**
- Redis configuration for rate limiting
- Knife4j API documentation aggregation
- Management endpoints and tracing
- Logging configuration with trace/span ID correlation

#### 3. **base-gateway.yml**
- Gateway discovery locator enabled (automatic route creation from Nacos)
- Default filters: TokenRelay, Retry, RequestRateLimiter
- OAuth2 resource server configuration with JWT issuer URI
- OAuth2 client configuration for authorization code flow
- Permit paths (whitelist): `/v3/**`, `/webjars/**`, `/assets/**`, `/doc.html`, `/favicon.ico`
- URL permission validation toggle: `opensabre.gateway.permission.enabled` (default: false)

## Key Features

### 1. **Dynamic URL Authorization**
- Role-based permission checking at URL level via `DynamicAuthorizationManager`
- Extracts authorities from JWT `roles` claim
- Whitelist configuration for public endpoints
- Toggle feature via `opensabre.gateway.permission.enabled` (disabled by default)

### 2. **OAuth2 Integration**
- JWT token validation with issuer URI: `http://www.opensabre.cloud:8000`
- Custom authority extraction from `roles` claim
- Resource server configuration for protected endpoints

### 3. **Rate Limiting**
- Redis-based token bucket algorithm
- Configurable via `redis-rate-limiter.replenishRate` and `redis-rate-limiter.burstCapacity`
- Uses `DefaultRedisRateLimiter` custom implementation

### 4. **Service Discovery**
- Nacos integration for automatic service registration/discovery
- Gateway discovery locator enabled for automatic route creation
- Lower-case service IDs for consistency

### 5. **API Documentation Aggregation**
- Knife4j integration for aggregating Swagger/OpenAPI documentation
- Accessible at: `http://localhost:8443/doc.html`
- Excludes gateway service itself from aggregation

### 6. **Observability**
- Spring Boot Actuator endpoints enabled
- Distributed tracing with OpenTelemetry (W3C propagation)
- Logging with trace/span ID correlation for request tracing

## Development Notes

### Security Implementation
- Uses Spring Security WebFlux with reactive authorization managers
- JWT-based authentication with custom authority extraction
- Dynamic URL-level authorization based on user roles
- Whitelist for static resources and API documentation

### Reactive Patterns
- Entire gateway is built on reactive Spring WebFlux
- Redis reactive client for rate limiting
- Reactive security chain for authorization

### Environment Variables
- `SERVER_PORT`: Application port (default: 8443)
- `REDIS_HOST`, `REDIS_PORT`: Redis connection
- `REGISTER_HOST`, `REGISTER_PORT`: Nacos connection
- `SENTINEL_DASHBOARD_HOST`, `SENTINEL_DASHBOARD_PORT`: Sentinel dashboard

### Docker Configuration
- Base image: `eclipse-temurin:21-jre-alpine`
- Compiles to Java 17 bytecode
- App root: `/app`
- User: `1001` with proper ownership for logs volume
- Timezone: `Asia/Shanghai`
- Logs volume: `/app/logs`
- Uses Jib plugin with ownership extension for volume permissions

## Testing
- Basic context loading test in `GatewayApplicationTests.java`
- Test configuration in `src/test/resources/application.yml`
- Uses `opensabre-test` dependency for test utilities

## Dependencies (Key)
- Spring Cloud Gateway
- Spring Cloud Alibaba (Nacos, Sentinel)
- Spring Security OAuth2 (Client + Resource Server)
- Spring Boot Starter Data Redis Reactive for rate limiting
- Caffeine for in-memory caching
- Knife4j for API documentation
- Jib for Docker image building
- OpenTelemetry for distributed tracing
- Spring Boot Actuator for monitoring