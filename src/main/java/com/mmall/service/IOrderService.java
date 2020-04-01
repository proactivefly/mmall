package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

  ServerResponse pay(Integer userId , Long orderNo, String path);

  ServerResponse alipayCallback(Map<String,String> params);

  ServerResponse createOrder(Integer userId, Integer shippingId);

  ServerResponse<String> cancel(Integer userId,long orderNo);

  ServerResponse getCartProductDetail(Integer userId);

  ServerResponse getOrderDetail(Integer userId,long orderNo);

  ServerResponse<PageInfo> getList(Integer userId, Integer pageNum, Integer pageSize);

  //后台
  ServerResponse<PageInfo> manageList(int pageNum,int pageSize);

  ServerResponse<OrderVo> manageDetail(Long orderNo);

  ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);

  ServerResponse<String> manageSendGoods(Long orderNo);

}
