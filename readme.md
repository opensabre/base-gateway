网关应用
----------

## 关键词

`路由、网关`

## 简介

网关应用，提供网关路由转发、降级、熔断、请求处理、接口文档聚合等网关功能。

## 启动

### 先决条件

- [redis](http://redis.io/download)
- [nacos](https://nacos.io)

### 启动命令

进入应用目录

启动命令：`mvn spring-boot:run`

docker镜像打包：`mvn docker:build`

## 使用指南

### 路由功能

### API文档聚合

网关默认聚合了所有已在网关中配置过路由的应用的swagger文档

默认地址：http://localhost:8443/doc.html