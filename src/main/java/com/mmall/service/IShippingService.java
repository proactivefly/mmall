package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;


public interface IShippingService {
  ServerResponse add(Integer userId, Shipping shipping);
  ServerResponse delete(Integer userId,Integer shippingId);
  ServerResponse update(Integer userId,Shipping shipping);
  ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);
}
