package com.mmall.vo;

import java.math.BigDecimal;

public class CartItemVo {
  private Integer id;

  private Integer userId;

  private Integer productId;

  private Integer quantity;

  private String productName;

  private Integer checked;//是否勾选

  private BigDecimal price;

  private String productSubTitle;

  private BigDecimal totalPrice;

  private Integer status;//产品状态

  private Integer stock;//库存

  private String mainImage;

  private String limitQuantity;//产品限制

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getProductId() {
    return productId;
  }

  public void setProductId(Integer productId) {
    this.productId = productId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Integer getChecked() {
    return checked;
  }

  public void setChecked(Integer checked) {
    this.checked = checked;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getProductSubTitle() {
    return productSubTitle;
  }

  public void setProductSubTitle(String productSubTitle) {
    this.productSubTitle = productSubTitle;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public String getMainImage() {
    return mainImage;
  }

  public void setMainImage(String mainImage) {
    this.mainImage = mainImage;
  }

  public String getLimitQuantity() {
    return limitQuantity;
  }

  public void setLimitQuantity(String limitQuantity) {
    this.limitQuantity = limitQuantity;
  }
}
