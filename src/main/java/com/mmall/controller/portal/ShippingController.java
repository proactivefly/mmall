package com.mmall.controller.portal;


import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")

public class ShippingController {
  @Autowired
  private IShippingService iShippingService;

  /**
   * 新增地址
   * @param session
   * @param shipping
   * @return
   */
  @RequestMapping(value="add.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse add(HttpSession session, Shipping shipping){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    return iShippingService.add(user.getId(),shipping);
  }

  /**
   * 删除地址
   * @param session
   * @param shippingId
   * @return
   */
  @RequestMapping(value="delete.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse delete(HttpSession session, Integer shippingId){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    return iShippingService.delete(user.getId(),shippingId);
  }

  /**
   * 更新地址
   * @param session
   * @param shipping
   * @return
   */
  @RequestMapping(value="update.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse update(HttpSession session, Shipping shipping){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    return iShippingService.update(user.getId(),shipping);
  }

  /**
   * 地址列表
   * @param pageNum
   * @param pageSize
   * @param session
   * @return
   */
  @RequestMapping(value="list.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<PageInfo> getList(@RequestParam(value="pageNum",defaultValue = "1") Integer pageNum,
                                          @RequestParam(value ="pageSize",defaultValue = "10") Integer pageSize,
                                          HttpSession session
                                          ){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    return iShippingService.list(pageNum,pageSize,user.getId());
  }
}
