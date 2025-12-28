# 二手交易平台 - 前端项目

## 项目简介

这是一个简单的二手交易平台前端页面，使用纯HTML + JavaScript开发，无需构建工具，可直接在浏览器中运行。

## 技术栈

- **HTML5**: 页面结构
- **Bootstrap 5.3**: 响应式UI框架
- **Font Awesome 6.4**: 图标库
- **Vanilla JavaScript**: 业务逻辑（无需Vue/React等框架）
- **Fetch API**: HTTP请求

## 功能特性

### 已实现功能

- ✅ 用户注册/登录
- ✅ JWT Token认证
- ✅ 商品列表展示
- ✅ 商品详情查看
- ✅ 发布商品
- ✅ 创建订单（购买商品）
- ✅ 查看我的订单
- ✅ 订单支付
- ✅ 响应式设计（支持移动端）

### 核心特点

- 🚀 **零配置**: 无需npm install，无需webpack
- 📱 **响应式**: 支持PC和移动端
- 🔒 **安全**: JWT Token认证，LocalStorage存储
- 🎨 **美观**: Bootstrap样式，现代化UI
- 💬 **友好**: Toast消息提示

## 快速开始

### 1. 确保后端服务已启动

```bash
cd /mnt/share/secondhand-microservices
docker-compose ps
```

确认所有服务都是 `Up (healthy)` 状态。

### 2. 启动前端

**方法1：使用Python HTTP服务器（推荐）**

```bash
cd /mnt/share/secondhand-microservices/frontend
python3 -m http.server 8000
```

然后访问: http://localhost:8000

**方法2：直接用浏览器打开**

```bash
firefox /mnt/share/secondhand-microservices/frontend/index.html
```

### 3. 开始使用

1. 注册新用户
2. 登录系统
3. 浏览商品或发布商品
4. 购买商品创建订单
5. 查看我的订单

## 文件说明

```
frontend/
├── index.html           # 主页面HTML，包含所有页面结构
├── app.js              # JavaScript业务逻辑
├── 前端测试文档.md      # 详细测试文档
└── README.md           # 项目说明（本文件）
```

## API接口

前端调用的后端API（基于实际代码）：

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 商品接口
- `GET /api/product` - 查询商品列表
- `GET /api/product/{id}` - 查询商品详情
- `POST /api/product` - 创建商品（需要JWT）

### 订单接口
- `POST /api/order` - 创建订单（需要JWT）
- `GET /api/order/buyer/{userId}` - 查询买家订单（需要JWT）
- `PUT /api/order/{id}/pay` - 订单支付（需要JWT）

所有API基础地址: `http://localhost:8080/api`

## 浏览器兼容性

- ✅ Chrome/Chromium 90+
- ✅ Firefox 88+
- ✅ Edge 90+
- ✅ Safari 14+

## 配置说明

### API基础URL

在 `app.js` 第2行：

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

如果后端部署在其他地址，修改此配置。

### 本地存储

使用 `localStorage` 存储用户信息：

- `token`: JWT认证令牌
- `username`: 用户名
- `userId`: 用户ID

## 开发调试

### 打开浏览器开发者工具

- 按 `F12` 或 `Ctrl+Shift+I`

### 查看网络请求

1. 切换到 **Network** 标签页
2. 执行操作（如登录）
3. 查看请求详情和响应

### 查看控制台

1. 切换到 **Console** 标签页
2. 查看JavaScript错误或日志

## 常见问题

### Q: 页面无法加载商品？

**A**: 检查后端服务是否启动:

```bash
docker-compose ps
curl http://localhost:8080/api/product
```

### Q: 登录后提示"未授权"？

**A**: 清空浏览器LocalStorage后重新登录:

1. 打开开发者工具 → Application → Local Storage
2. 右键 → Clear
3. 刷新页面重新登录

### Q: CORS跨域错误？

**A**: 使用Python HTTP服务器启动，不要直接打开HTML文件:

```bash
cd frontend
python3 -m http.server 8000
```

## 测试文档

详细的测试流程请查看: [前端测试文档.md](./前端测试文档.md)

包含：
- 完整功能测试流程
- 端到端测试场景
- 调试技巧
- 常见问题解决方案

## 未来改进

- [ ] 商品图片上传
- [ ] 评论功能
- [ ] 搜索和筛选
- [ ] 分页加载
- [ ] 订单详情展示商品信息
- [ ] 个人中心
- [ ] 卖家订单管理

## 许可证

MIT License

---

**开发环境**: Ubuntu 24.04
**后端项目**: 二手交易平台微服务系统
**文档更新**: 2025-12-27
