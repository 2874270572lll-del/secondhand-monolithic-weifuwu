package com.zjgsu.lll.secondhand.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建事件
 * 当订单创建成功后，发送此事件到消息队列
 */
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String contactPhone;
    private LocalDateTime createTime;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, String orderNo, Long buyerId, Long sellerId,
                           Long productId, BigDecimal totalAmount, String shippingAddress,
                           String contactPhone, LocalDateTime createTime) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.contactPhone = contactPhone;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", productId=" + productId +
                ", totalAmount=" + totalAmount +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
