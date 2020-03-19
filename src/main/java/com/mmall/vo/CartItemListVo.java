package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartItemListVo {
  private List<CartItemVo> cartItemVoList;//cartItemVO集合
  private BigDecimal totalPrice;
  private Boolean allChecked; //是否全部勾选
  private String imageHost;

  public List<CartItemVo> getCartItemVoList() {
    return cartItemVoList;
  }

  public void setCartItemVoList(List<CartItemVo> cartItemVoList) {
    this.cartItemVoList = cartItemVoList;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  public Boolean getAllChecked() {
    return allChecked;
  }

  public void setAllChecked(Boolean allChecked) {
    this.allChecked = allChecked;
  }

  public String getImageHost() {
    return imageHost;
  }

  public void setImageHost(String imageHost) {
    this.imageHost = imageHost;
  }
}
