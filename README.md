# Warehouse Oracle 仓库系统

基于 Spring Boot + MyBatis-Plus + Shiro 的仓库管理系统，支持资产/耗材管理、出入库、盘点等业务能力，并预留标准 HTTP+JSON API 供企业内部系统对接。

## 技术栈

- Java 8 / Spring Boot 2.2.x
- MyBatis-Plus + Oracle
- Apache Shiro（会话登录 + 权限控制）
- Swagger2（接口文档）

## 快速开始

### 1) 配置

关键配置位于 [application.yml](file:///c:/Users/0057/Documents/warehouse-oracle/src/main/resources/application.yml)：

- 服务端口：`server.port`（默认 8888）
- 数据库：`spring.datasource.druid.*`
- Shiro 放行路径：`shiro.anon-urls`
- 对外 API Token：`api.token`（对接系统需在请求头携带 `Authorization`）

### 2) 在 Linux 服务器上启动

- mkdir -p /opt/warehouse-oracle/upload/
  chmod -R 777 /opt/warehouse-oracle/upload/
  chmod -R 755 /opt/warehouse-oracle/upload/
- 启动服务：

```bash
nohup java -jar warehouse-0.X.X.jar > date.log 2>&1 &
```
```bash
#查询端口状态信息
firewall-cmd --query-port=8888/tcp
#开启端口
firewall-cmd --zone=public --add-port=888/tcp --permanent
#重新载入规则
firewall-cmd --reload
#如果要列出所有端口查看，可以使用：
firewall-cmd --list-ports
```

## 对外对接 API

系统提供 `/api/**` 路径用于内部系统对接（HTTP/JSON）。接口自身包含 Token 鉴权：请求头必须携带 `Authorization: <api.token>`。

- 获取设备信息（按运维编号列表查询）
  - `POST /api/device/info`
  - 请求体：运维编号数组（JSON）
  - 返回：设备信息列表（运维编号、设备类型、品牌、序列号、资产编号、位置、状态、入库时间）

实现位置： [ApiController.java](file:///c:/Users/0057/Documents/warehouse-oracle/src/main/java/com/yeqifu/bus/controller/ApiController.java)
