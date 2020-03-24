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

  /**
   * 更新购物车
   * @param session
   * @param productId
   * @param count
   * @return
   */
  @RequestMapping(value="update.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> update(HttpSession session, Integer productId, Integer count){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.update(user.getId(),productId,count);
  }

  /**
   * 从购物车中删除商品
   * @param session
   * @param productIds
   * @return
   */
  @RequestMapping(value="delete.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> delete(HttpSession session, String productIds){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.delete(user.getId(),productIds);
  }

  /**
   * 获取购物车列表
   * @param session
   * @return
   */
  @RequestMapping(value="list.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> list(HttpSession session){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.getList(user.getId());
  }

  /**
   * 全选
   * @param session
   * @return
   */
  @RequestMapping(value="checkedAll.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> checkedAll(HttpSession session){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.checkOrUnCheck(user.getId(),Const.Cart.CHECK_ON,null);
  }

  /**
   * 全反选
   * @param session
   * @return
   */
  @RequestMapping(value="unCheckedAll.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> unCheckedAll(HttpSession session){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.checkOrUnCheck(user.getId(),Const.Cart.CHECK_OFF,null);
  }

  /**
   * 单选
   * @param session
   * @param productId
   * @return
   */
  @RequestMapping(value="check.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> check(HttpSession session,Integer productId){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.checkOrUnCheck(user.getId(),Const.Cart.CHECK_ON,productId);
  }

  /**
   * 多选
   * @param session
   * @param productId
   * @return
   */
  @RequestMapping(value="unCheck.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<CartItemListVo> unCheck(HttpSession session,Integer productId){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iCartService.checkOrUnCheck(user.getId(),Const.Cart.CHECK_ON,productId);
  }

  /**
   * 获取购物车总量，用于显示角标
   * @param session
   * @return
   */
  @RequestMapping(value="get_cart_num.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<Integer> getCartNum(HttpSession session){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createBySuccess(0);
    }
    return iCartService.getCartNum(user.getId());
  }

}
