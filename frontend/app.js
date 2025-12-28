// API基础URL
const API_BASE_URL = 'http://localhost:8080/api';

// 全局状态
let currentUser = null;
let currentProduct = null;
let currentUserInfo = null; // 存储当前用户的完整信息（用于编辑）

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    checkLogin();
    loadProducts();
    initEventListeners();
});

// 检查登录状态
function checkLogin() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const userId = localStorage.getItem('userId');

    if (token && username && userId) {
        currentUser = { token, username, userId: parseInt(userId) };
        updateNavbar(true);
    } else {
        updateNavbar(false);
        showPage('login');
    }
}

// 更新导航栏
function updateNavbar(isLoggedIn) {
    const guestItems = document.querySelectorAll('.guest-only');
    const userItems = document.querySelectorAll('.user-only');
    const usernameDisplay = document.getElementById('username-display');

    if (isLoggedIn) {
        guestItems.forEach(item => item.style.display = 'none');
        userItems.forEach(item => item.style.display = 'block');
        usernameDisplay.innerHTML = `<i class="fas fa-user"></i> ${currentUser.username}`;
    } else {
        guestItems.forEach(item => item.style.display = 'block');
        userItems.forEach(item => item.style.display = 'none');
    }
}

// 页面切换
function showPage(pageName) {
    // 隐藏所有页面
    document.querySelectorAll('.page-section').forEach(section => {
        section.classList.remove('active');
    });

    // 显示目标页面
    const targetPage = document.getElementById(pageName + '-page');
    if (targetPage) {
        targetPage.classList.add('active');

        // 加载对应数据
        if (pageName === 'products') {
            loadProducts();
        } else if (pageName === 'orders') {
            loadOrders();
        } else if (pageName === 'profile') {
            loadProfile();
        }
    }
}

// 初始化事件监听
function initEventListeners() {
    // 登录表单
    document.getElementById('login-form').addEventListener('submit', handleLogin);

    // 注册表单
    document.getElementById('register-form').addEventListener('submit', handleRegister);

    // 发布商品表单
    document.getElementById('publish-form').addEventListener('submit', handlePublish);
}

// 处理登录
async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            mode: 'cors',  // 添加这行
            credentials: 'include',  // 添加这行
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const result = await response.json();

        if (result.code === 200) {
            const { token, username, userId } = result.data;

            // 保存到localStorage
            localStorage.setItem('token', token);
            localStorage.setItem('username', username);
            localStorage.setItem('userId', userId);

            currentUser = { token, username, userId: parseInt(userId) };
            updateNavbar(true);
            showPage('products');
            showSuccess('登录成功!');
        } else {
            showError(result.message || '登录失败');
        }
    } catch (error) {
        console.error('登录错误:', error);
        showError('登录失败,请检查网络连接');
    }
}

// 处理注册
async function handleRegister(e) {
    e.preventDefault();

    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('注册成功！请登录');
            showPage('login');
            // 自动填充用户名
            document.getElementById('login-username').value = username;
        } else {
            showError(result.message || '注册失败');
        }
    } catch (error) {
        console.error('注册错误:', error);
        showError('注册失败，请检查网络连接');
    }
}

// 加载商品列表
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/product`);
        const result = await response.json();

        if (result.code === 200) {
            displayProducts(result.data);
        } else {
            showError('加载商品失败');
        }
    } catch (error) {
        console.error('加载商品错误:', error);
        showError('加载商品失败，请检查网络连接');
    }
}

// 显示商品列表
function displayProducts(products) {
    const container = document.getElementById('products-container');

    if (!products || products.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                <p class="text-muted">暂无商品</p>
            </div>
        `;
        return;
    }

    container.innerHTML = products.map(product => `
        <div class="col-md-4 col-lg-3">
            <div class="card product-card" onclick="showProductDetail(${product.id})">
                <div class="product-img">
                    <i class="fas fa-box-open"></i>
                </div>
                <div class="card-body">
                    <h5 class="card-title text-truncate" title="${product.name}">${product.name}</h5>
                    <p class="card-text text-muted small text-truncate" title="${product.description}">
                        ${product.description || '暂无描述'}
                    </p>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="price">¥${product.price}</span>
                        <span class="badge bg-secondary">${product.category}</span>
                    </div>
                    <div class="mt-2">
                        <small class="text-muted">库存: ${product.stock}</small>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// 显示商品详情
async function showProductDetail(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/product/${productId}`);
        const result = await response.json();

        if (result.code === 200) {
            currentProduct = result.data;

            // 如果用户已登录，检查是否有已支付的订单
            let canComment = false;
            if (currentUser) {
                canComment = await checkCanComment(productId);
            }

            displayProductDetail(result.data, canComment);
            showPage('product-detail');
        } else {
            showError('加载商品详情失败');
        }
    } catch (error) {
        console.error('加载商品详情错误:', error);
        showError('加载商品详情失败');
    }
}

// 检查用户是否可以评论（是否有已支付的订单）
async function checkCanComment(productId) {
    if (!currentUser) {
        return false;
    }

    try {
        // 查询用户的买家订单
        const response = await fetch(`${API_BASE_URL}/order/buyer/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            // 检查是否有该商品的已支付订单（status >= 1）
            const hasOrders = result.data.some(order =>
                order.productId === productId && order.status >= 1
            );
            return hasOrders;
        }
        return false;
    } catch (error) {
        console.error('检查评论权限错误:', error);
        return false;
    }
}

// 显示商品详情内容
function displayProductDetail(product, canComment = false) {
    const container = document.getElementById('product-detail-container');

    container.innerHTML = `
        <div class="card shadow">
            <div class="row g-0">
                <div class="col-md-5">
                    <div class="product-img" style="height: 400px; border-radius: 0.375rem 0 0 0.375rem;">
                        <i class="fas fa-box-open"></i>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="card-body p-4">
                        <h2 class="mb-3">${product.name}</h2>
                        <div class="mb-3">
                            <span class="badge bg-primary">${product.category}</span>
                            <span class="badge bg-success ms-2">库存: ${product.stock}</span>
                        </div>
                        <div class="price mb-4">¥${product.price}</div>
                        <div class="mb-4">
                            <h5>商品描述</h5>
                            <p class="text-muted">${product.description || '暂无描述'}</p>
                        </div>
                        <div class="mb-4">
                            <small class="text-muted">
                                发布时间: ${new Date(product.createTime).toLocaleString('zh-CN')}
                            </small>
                        </div>
                        ${currentUser ? `
                            <button class="btn btn-primary btn-lg" onclick="buyProduct(${product.id})">
                                <i class="fas fa-shopping-cart"></i> 立即购买
                            </button>
                        ` : `
                            <button class="btn btn-secondary btn-lg" onclick="showPage('login')">
                                <i class="fas fa-sign-in-alt"></i> 登录后购买
                            </button>
                        `}
                    </div>
                </div>
            </div>
        </div>

        <!-- 评论区域 -->
        <div class="card shadow mt-4">
            <div class="card-body">
                <h4 class="mb-4"><i class="fas fa-comments"></i> 商品评价</h4>

                <!-- 发布评论表单 -->
                ${canComment ? `
                    <div class="card bg-light mb-4">
                        <div class="card-body">
                            <h6 class="mb-3">发表评价</h6>
                            <form id="comment-form" onsubmit="submitComment(event, ${product.id})">
                                <div class="mb-3">
                                    <label class="form-label">评分</label>
                                    <div class="rating-input">
                                        ${[5,4,3,2,1].map(star => `
                                            <input type="radio" name="rating" id="star${star}" value="${star}" required>
                                            <label for="star${star}" title="${star}星">
                                                <i class="fas fa-star"></i>
                                            </label>
                                        `).join('')}
                                    </div>
                                    <style>
                                        .rating-input {
                                            display: flex;
                                            flex-direction: row-reverse;
                                            justify-content: flex-end;
                                            gap: 5px;
                                        }
                                        .rating-input input[type="radio"] {
                                            display: none;
                                        }
                                        .rating-input label {
                                            cursor: pointer;
                                            font-size: 1.5rem;
                                            color: #ddd;
                                        }
                                        .rating-input input[type="radio"]:checked ~ label,
                                        .rating-input label:hover,
                                        .rating-input label:hover ~ label {
                                            color: #ffc107;
                                        }
                                    </style>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">评价内容</label>
                                    <textarea class="form-control" id="comment-content" rows="3"
                                              placeholder="分享你的购买体验..." required></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-paper-plane"></i> 发布评价
                                </button>
                            </form>
                        </div>
                    </div>
                ` : currentUser ? `
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> 购买并支付订单后，您可以对商品进行评价
                    </div>
                ` : ''}

                <!-- 评论列表 -->
                <div id="comments-container">
                    <div class="text-center py-3">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">加载中...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    // 加载评论列表
    loadComments(product.id);
}

// 购买商品（创建订单）
async function buyProduct(productId) {
    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    // 简单的购买确认
    const confirmed = confirm(`确认购买 ${currentProduct.name}？\n价格: ¥${currentProduct.price}`);
    if (!confirmed) return;

    try {
        const response = await fetch(`${API_BASE_URL}/order`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({
                userId: currentUser.userId,
                productId: productId,
                quantity: 1,
                totalPrice: currentProduct.price,
                shippingAddress: '默认地址',
                contactPhone: '13800138000'
            })
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('订单创建成功！');
            showPage('orders');
        } else {
            showError(result.message || '创建订单失败');
        }
    } catch (error) {
        console.error('创建订单错误:', error);
        showError('创建订单失败');
    }
}

// 处理发布商品
async function handlePublish(e) {
    e.preventDefault();

    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    const name = document.getElementById('product-name').value;
    const description = document.getElementById('product-description').value;
    const price = parseFloat(document.getElementById('product-price').value);
    const stock = parseInt(document.getElementById('product-stock').value);
    const category = document.getElementById('product-category').value;

    try {
        const response = await fetch(`${API_BASE_URL}/product`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({
                name,
                description,
                price,
                stock,
                category,
                sellerId: currentUser.userId
            })
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('商品发布成功！');
            document.getElementById('publish-form').reset();
            showPage('products');
        } else {
            showError(result.message || '发布失败');
        }
    } catch (error) {
        console.error('发布商品错误:', error);
        showError('发布失败，请检查网络连接');
    }
}

// 加载我的订单
async function loadOrders() {
    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/order/buyer/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            displayOrders(result.data);
        } else {
            showError('加载订单失败');
        }
    } catch (error) {
        console.error('加载订单错误:', error);
        showError('加载订单失败');
    }
}

// 显示订单列表
function displayOrders(orders) {
    const container = document.getElementById('orders-container');

    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div class="text-center py-5">
                <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                <p class="text-muted">暂无订单</p>
            </div>
        `;
        return;
    }

    const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消'
    };

    container.innerHTML = orders.map(order => `
        <div class="card mb-3">
            <div class="card-body">
                <div class="row">
                    <div class="col-md-8">
                        <h5>订单号: ${order.orderNo}</h5>
                        <p class="text-muted mb-1">商品ID: ${order.productId}</p>
                        <p class="text-muted mb-1">下单时间: ${new Date(order.createTime).toLocaleString('zh-CN')}</p>
                        <p class="text-muted mb-0">配送地址: ${order.shippingAddress || '无'}</p>
                    </div>
                    <div class="col-md-4 text-end">
                        <div class="price mb-2">¥${order.totalAmount}</div>
                        <span class="order-status status-${order.status}">${statusMap[order.status]}</span>
                        ${order.status === 0 ? `
                            <div class="mt-2">
                                <button class="btn btn-sm btn-success me-2" onclick="payOrder(${order.id})">
                                    <i class="fas fa-credit-card"></i> 支付
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="cancelOrder(${order.id})">
                                    <i class="fas fa-times"></i> 取消
                                </button>
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// 支付订单
async function payOrder(orderId) {
    if (!currentUser) {
        showError('请先登录');
        return;
    }

    const confirmed = confirm('确认支付该订单？');
    if (!confirmed) return;

    try {
        const response = await fetch(`${API_BASE_URL}/order/${orderId}/pay`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('支付成功！');
            loadOrders();
        } else {
            showError(result.message || '支付失败');
        }
    } catch (error) {
        console.error('支付订单错误:', error);
        showError('支付失败');
    }
}

// 取消订单
async function cancelOrder(orderId) {
    if (!currentUser) {
        showError('请先登录');
        return;
    }

    const confirmed = confirm('确认取消该订单？');
    if (!confirmed) return;

    try {
        const response = await fetch(`${API_BASE_URL}/order/${orderId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('订单已取消');
            loadOrders();
        } else {
            showError(result.message || '取消订单失败');
        }
    } catch (error) {
        console.error('取消订单错误:', error);
        showError('取消订单失败');
    }
}

// 加载个人中心
async function loadProfile() {
    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    try {
        // 加载用户信息 - 直接使用/users路径（UserController的实际路径）
        const userResponse = await fetch(`${API_BASE_URL}/users/${currentUser.userId}`, {
            mode: 'cors',
            credentials: 'include',
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const userResult = await userResponse.json();

        if (userResult.code === 200) {
            currentUserInfo = userResult.data; // 保存用户信息到全局变量
            displayUserInfo(userResult.data);
            // 加载统计信息（静默失败，不影响主功能）
            loadProfileStats();
        } else {
            showError('加载用户信息失败: ' + (userResult.message || '未知错误'));
            console.error('User info load failed:', userResult);
        }

    } catch (error) {
        console.error('加载个人信息错误:', error);
        showError('加载个人信息失败，请检查网络连接');
    }
}

// 显示用户信息
function displayUserInfo(user) {
    const container = document.getElementById('user-info-container');

    const statusText = user.status === 1 ? '正常' : '禁用';
    const statusClass = user.status === 1 ? 'success' : 'danger';

    container.innerHTML = `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="text-muted small">用户ID</label>
                <div class="fw-bold">${user.id}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">用户名</label>
                <div class="fw-bold">${user.username}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">邮箱</label>
                <div class="fw-bold">${user.email || '未设置'}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">手机号</label>
                <div class="fw-bold">${user.phone || '未设置'}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">地址</label>
                <div class="fw-bold">${user.address || '未设置'}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">账号状态</label>
                <div>
                    <span class="badge bg-${statusClass}">${statusText}</span>
                </div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">注册时间</label>
                <div class="fw-bold">${new Date(user.createTime).toLocaleString('zh-CN')}</div>
            </div>
            <div class="col-md-6 mb-3">
                <label class="text-muted small">最后更新</label>
                <div class="fw-bold">${new Date(user.updateTime).toLocaleString('zh-CN')}</div>
            </div>
            <div class="col-12 mb-3">
                <button class="btn btn-outline-primary btn-sm" onclick="openEditAddressModal()">
                    <i class="fas fa-edit"></i> 编辑地址信息
                </button>
            </div>
        </div>
    `;
}

// 加载统计信息
async function loadProfileStats() {
    if (!currentUser) return;

    // 设置默认值
    document.getElementById('stat-orders').textContent = '0';
    document.getElementById('stat-products').textContent = '0';
    document.getElementById('stat-sales').textContent = '0';

    try {
        // 加载买家订单数量
        console.log('Loading buyer orders...');
        const buyerOrdersResponse = await fetch(`${API_BASE_URL}/order/buyer/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });
        const buyerOrdersResult = await buyerOrdersResponse.json();
        const buyerOrdersCount = buyerOrdersResult.code === 200 ? buyerOrdersResult.data.length : 0;
        document.getElementById('stat-orders').textContent = buyerOrdersCount;
        console.log('Buyer orders:', buyerOrdersCount);

        // 加载卖家订单数量
        console.log('Loading seller orders...');
        const sellerOrdersResponse = await fetch(`${API_BASE_URL}/order/seller/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });
        const sellerOrdersResult = await sellerOrdersResponse.json();
        const sellerOrdersCount = sellerOrdersResult.code === 200 ? sellerOrdersResult.data.length : 0;
        document.getElementById('stat-sales').textContent = sellerOrdersCount;
        console.log('Seller orders:', sellerOrdersCount);

        // 加载发布商品数量
        console.log('Loading products...');
        const productsResponse = await fetch(`${API_BASE_URL}/product/seller/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });
        const productsResult = await productsResponse.json();
        const productsCount = productsResult.code === 200 ? productsResult.data.length : 0;
        document.getElementById('stat-products').textContent = productsCount;
        console.log('Products:', productsCount);

    } catch (error) {
        console.error('加载统计信息错误:', error);
        // 静默失败，不影响用户体验
    }
}

// 加载我发布的商品
async function loadMyProducts() {
    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/product/seller/${currentUser.userId}`, {
            headers: {
                'Authorization': `Bearer ${currentUser.token}`
            }
        });

        const result = await response.json();

        if (result.code === 200) {
            // 先手动切换到商品页面（不触发自动加载）
            document.querySelectorAll('.page-section').forEach(section => {
                section.classList.remove('active');
            });
            document.getElementById('products-page').classList.add('active');

            // 然后显示我发布的商品
            displayProducts(result.data);
            showSuccess(`找到${result.data.length}个我发布的商品`);
        } else {
            showError('加载商品失败');
        }
    } catch (error) {
        console.error('加载商品错误:', error);
        showError('加载商品失败');
    }
}

// 退出登录
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    currentUser = null;
    updateNavbar(false);
    showPage('login');
    showSuccess('已退出登录');
}

// 显示成功消息
function showSuccess(message) {
    showToast(message, 'success');
}

// 显示错误消息
function showError(message) {
    showToast(message, 'danger');
}

// 显示Toast消息
function showToast(message, type) {
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type} border-0 position-fixed top-0 start-50 translate-middle-x mt-3"
             role="alert" style="z-index: 9999;">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    const toastElement = document.createElement('div');
    toastElement.innerHTML = toastHtml;
    document.body.appendChild(toastElement);

    const toast = new bootstrap.Toast(toastElement.querySelector('.toast'), {
        autohide: true,
        delay: 3000
    });

    toast.show();

    // 3秒后移除元素
    setTimeout(() => {
        toastElement.remove();
    }, 3500);
}

// 打开编辑地址模态框
function openEditAddressModal() {
    if (!currentUser) {
        showError('请先登录');
        return;
    }

    if (!currentUserInfo) {
        showError('用户信息未加载');
        return;
    }

    // 获取当前用户的地址信息（从后端数据）
    const phone = currentUserInfo.phone || '';
    const address = currentUserInfo.address || '';

    // 填充表单
    document.getElementById('edit-phone').value = phone;
    document.getElementById('edit-address').value = address;

    // 打开模态框
    const modal = new bootstrap.Modal(document.getElementById('editAddressModal'));
    modal.show();
}

// 保存地址信息
async function saveAddressInfo() {
    if (!currentUser) {
        showError('请先登录');
        return;
    }

    if (!currentUserInfo) {
        showError('用户信息未加载');
        return;
    }

    // 获取表单值
    const phone = document.getElementById('edit-phone').value.trim();
    const address = document.getElementById('edit-address').value.trim();

    // 验证手机号格式（如果填写了）
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
        showError('请输入有效的手机号（11位数字）');
        return;
    }

    try {
        // 调用后端API更新用户信息
        const response = await fetch(`${API_BASE_URL}/users/${currentUser.userId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({
                username: currentUserInfo.username,
                email: currentUserInfo.email,
                phone: phone || null,
                address: address || null,
                password: '' // 不修改密码，发送空字符串
            })
        });

        const result = await response.json();

        if (result.code === 200) {
            // 更新全局用户信息
            currentUserInfo = result.data;

            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('editAddressModal'));
            modal.hide();

            // 重新加载个人信息页面以显示更新后的数据
            loadProfile();

            showSuccess('地址信息已保存');
        } else {
            showError(result.message || '保存失败');
        }
    } catch (error) {
        console.error('保存地址信息错误:', error);
        showError('保存失败，请检查网络连接');
    }
}

// 加载商品评论
async function loadComments(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/comment/product/${productId}`);
        const result = await response.json();

        if (result.code === 200) {
            displayComments(result.data);
        } else {
            showError('加载评论失败');
            document.getElementById('comments-container').innerHTML = `
                <div class="text-center py-3 text-muted">
                    <i class="fas fa-exclamation-circle"></i> 加载评论失败
                </div>
            `;
        }
    } catch (error) {
        console.error('加载评论错误:', error);
        document.getElementById('comments-container').innerHTML = `
            <div class="text-center py-3 text-muted">
                <i class="fas fa-exclamation-circle"></i> 加载评论失败
            </div>
        `;
    }
}

// 显示评论列表
function displayComments(comments) {
    const container = document.getElementById('comments-container');

    if (!comments || comments.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="fas fa-inbox fa-2x mb-2"></i>
                <p>暂无评价，快来抢沙发吧！</p>
            </div>
        `;
        return;
    }

    // 按时间倒序排列
    comments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

    container.innerHTML = comments.map(comment => `
        <div class="card mb-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <strong><i class="fas fa-user-circle"></i> 用户${comment.userId}</strong>
                        <div class="text-warning mt-1">
                            ${generateStarRating(comment.rating)}
                        </div>
                    </div>
                    <small class="text-muted">
                        ${new Date(comment.createTime).toLocaleString('zh-CN')}
                    </small>
                </div>
                <p class="mb-0">${comment.content}</p>
            </div>
        </div>
    `).join('');
}

// 生成星级评分HTML
function generateStarRating(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="fas fa-star"></i> ';
        } else {
            stars += '<i class="far fa-star"></i> ';
        }
    }
    return stars;
}

// 提交评论
async function submitComment(event, productId) {
    event.preventDefault();

    if (!currentUser) {
        showError('请先登录');
        showPage('login');
        return;
    }

    // 获取表单数据
    const rating = document.querySelector('input[name="rating"]:checked')?.value;
    const content = document.getElementById('comment-content').value.trim();

    if (!rating) {
        showError('请选择评分');
        return;
    }

    if (!content) {
        showError('请填写评价内容');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/comment`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({
                productId: productId,
                userId: currentUser.userId,
                orderId: 0, // 简化处理，实际应该关联具体订单
                content: content,
                rating: parseInt(rating)
            })
        });

        const result = await response.json();

        if (result.code === 200) {
            showSuccess('评价发布成功！');
            // 清空表单
            document.getElementById('comment-form').reset();
            // 重新加载评论列表
            loadComments(productId);
        } else {
            showError(result.message || '发布评价失败');
        }
    } catch (error) {
        console.error('发布评价错误:', error);
        showError('发布评价失败，请检查网络连接');
    }
}
