package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartItemListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
@Controller
@RequestMapping("/cart/")
public class CartController {
  @Autowired
  private ICartService iCartService;


  /**
   * 添加到购物车方法
   * @param session
   * @param productId
   * @param count
   * @return
   */
  @RequestMapping(value="add.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> add(HttpSession session, Integer productId, Integer count){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.add(user.getId(),productId,count);
  }
}
