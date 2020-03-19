package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartItemListVo;

public interface ICartService {
  ServerResponse<CartItemListVo> add(Integer userId, Integer productId, Integer count);
}
