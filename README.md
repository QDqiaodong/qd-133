# qd-133 马术俱乐部训练障碍杆骑手训练等级匹配管理系统

## 项目简介

马术俱乐部训练障碍杆、骑手与训练等级匹配管理系统。项目包含 Vue/Vite 前端、Spring Boot 后端、MySQL 与 Redis，已按依赖层缓存和固定端口交付链路规范整理。

## 访问地址

- 前端地址: [http://localhost:8153](http://localhost:8153)
- 127.0.0.1 地址: [http://127.0.0.1:8153](http://127.0.0.1:8153)
- 后端 API: http://localhost:8183/api

## 端口

- 前端: 8153
- 后端: 8183
- MySQL: 3369
- Redis: 6452

## 编译与启动

```bash
cd backend
mvn compile -q

cd ../frontend
npm ci
npm run build

cd ..
docker compose up -d --build
```

Docker Compose 端口均绑定到 `127.0.0.1`，镜像基础地址通过 `.env` 中的 `DOCKER_REGISTRY` 统一控制。

## 杆高复核

教练在「杆高复核」页填写**实测高度**与**复测人**后提交，系统按俱乐部约定（实测与标称相差超过 **5cm**，见 `HeightRecheckRules.RECHECK_TOLERANCE_CM`）判定：

- 差值 ≤ 5cm：结论「高度相符」，允许绑上训练位；
- 差值 > 5cm：结论「高度不符」，**拦住绑定**；若该杆已绑在训练位上会被立即拆下；
- 未做复核：禁止绑定。

复核结论、高度差、复测人、复核时间与 `bindable` 绑定资格均由后端计算并持久化（JPA `ddl-auto: update` 自动加列），刷新页面后结论与能否绑定保持一致。列表支持「全部 / 已复核 / 未复核」筛选；绑定拦截在训练位创建、编辑、绑定三个入口的服务端统一校验（前端下拉同步置灰）。档案标称高度被修改时，原复核结论失效并拆下训练位，须重新复核。

接口：

- `GET /api/recheck?rechecked=true|false` 复核列表（可按是否已复核筛选）
- `POST /api/recheck/{equipmentId}` 提交复核（`{ "measuredHeight": 41.0, "reviewer": "陈教练" }`）

