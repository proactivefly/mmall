package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartItemListVo;

public interface ICartService {
  ServerResponse<CartItemListVo> add(Integer userId, Integer productId, Integer count);
  ServerResponse<CartItemListVo> update(Integer userId,Integer productId,Integer count);
  ServerResponse<CartItemListVo> delete(Integer userId,String productIds);
  ServerResponse<CartItemListVo> getList(Integer userId);
  ServerResponse<CartItemListVo> checkOrUnCheck(Integer userId, Integer checked,Integer productId);
  ServerResponse<Integer> getCartNum(Integer userId);
}
