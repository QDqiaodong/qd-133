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

## 训练位占用

「训练位一览」按是否挂上骑手标记状态：已挂骑手的位标**占用**，没挂人的标**空闲**；页首统计占用数与空闲数，两数相加等于训练位总数（统计由后端 `GET /api/station/summary` 统一计算）。从某个位「拿下骑手」后（`POST /api/station/{id}/unbind-rider`，杆仍留在位上），该位立即改标空闲，统计同步更新。

接口：

- `GET /api/station/summary` 训练位占用统计（`{ "total": 5, "occupied": 3, "free": 2 }`）
- `POST /api/station/{id}/unbind-rider` 从训练位拿下骑手，该位改标空闲

## 骑手改级

教练在骑手档案「升级」时须填**改级原因**与**操作人**，少写一样前后端都会提示还没写全，不予提交。改级是幂等的：同一骑手同一新等级只生效一次——网络卡顿连点/重试时，改级全程锁住骑手档案行（`SELECT ... FOR UPDATE`），第二单排队读到新等级后直接返回当前状态，不会再记一条变更；`level_change_log` 另有 `(rider_id, new_level)` 唯一约束兜底（等级只升不降，同骑手同新等级本就只应出现一次）。

改级生效后，训练位上杆的适配等级若已高于骑手新等级，该骑手-杆组合在「训练位一览」的**等级适配**列标为**等级不符**（适配则标「适配」，缺骑手或缺杆显示「-」）；该标记由后端按骑手当前等级与杆适配等级实时推导（`TrainingStationResponse.levelMatch`），改级、换杆后刷新即准。

接口：

- `POST /api/rider/level/update` 改级（`{ "riderId": 1, "newLevel": 2, "changeReason": "考核通过", "operator": "陈教练" }`，幂等）
- `GET /api/rider/{id}/logs` 骑手等级变更记录

## 课后点评

教练在「课后点评」页**选好骑手**、写下**本节重点**并打**一到五星**后提交，三项缺一不可（前后端都会校验提示）。点评明细落库（`session_review` 表，JPA `ddl-auto: update` 自动建表）持久化，刷新页面后明细与汇总都还在。

星级汇总（总条数、平均星级、一到五星各星级条数）由后端 `GET /api/review/summary` 对**同一张表**实时 `GROUP BY` 统计，总条数取各星级条数之和，与点评明细条数同源，两处必然保持一致；页面上同时展示「明细 N 条 / 汇总 N 条」并标记是否一致。没评过的星级补 0，汇总固定返回 5 桶。

接口：

- `POST /api/review` 提交点评（`{ "riderId": 1, "sessionFocus": "障碍杆起跳节奏", "starRating": 5 }`）
- `GET /api/review` 点评明细列表（最新在前）
- `GET /api/review/summary` 按星级统计汇总（`{ "total": 4, "averageStars": 3.5, "starCounts": [{ "stars": 1, "count": 0 }, ...] }`）

## 骑手体测日

教练在骑手档案的「编辑」窗里选好骑手后，用日期选择器写下**最近一次体测日**并保存。该日期随骑手档案一起落库（`rider.last_fitness_test_date`，JPA `ddl-auto: update` 自动加列），保存成功后列表立即刷新，「最近体测日」列当场显示该日（没填过显示「未记录」）；关掉页面再打开，`GET /api/rider` 仍从库里读出该日期，不会只停在编辑窗里。字段可空，留空保存即清除已有日期。

接口（复用骑手档案接口，字段在骑手对象上）：

- `POST /api/rider` / `PUT /api/rider/{id}` 新增/编辑骑手（`{ ..., "lastFitnessTestDate": "2026-09-10" }`，格式 `YYYY-MM-DD`，可空）
- `GET /api/rider` 骑手列表，每个骑手带 `lastFitnessTestDate`

