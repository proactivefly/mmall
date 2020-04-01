package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

  @Autowired
  private IOrderService iOrderService;

  private static final Logger logger= LoggerFactory.getLogger(OrderController.class);
  /**
   * 生成支付二维码
   * @param session
   * @param shippingId
   * @return
   */

  @RequestMapping(value="create.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse createOrder(HttpSession session, Integer shippingId){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }

    return iOrderService.createOrder(user.getId(),shippingId);
  }

  /**
   * 取消订单
   * @param session
   * @param orderNo
   * @return
   */
  @RequestMapping(value="cancel.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse createOrder(HttpSession session, long orderNo){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.cancel(user.getId(),orderNo);
  }

  /**
   * 获取购物车种已经选中的商品详情
   * @param session
   * @return
   */
  @RequestMapping(value="get_order_cart_product.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse getCartProduct(HttpSession session){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getCartProductDetail(user.getId());
  }


  /**
   * 获取订单详情
   * @param session
   * @return
   */
  @RequestMapping(value="detail.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse detail(HttpSession session,long orderNo){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getOrderDetail(user.getId(),orderNo);
  }


  /**
   * 获取订单列表
   * @param session
   * @param pageNum
   * @param pageSize
   * @return
   */
  @RequestMapping(value="list.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse list(HttpSession session, @RequestParam(value="pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value="pageSize",defaultValue = "10") Integer pageSize){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    return iOrderService.getList(user.getId(),pageNum,pageSize);
  }



  /**
   * 支付接口
   * @param session
   * @param orderNo
   * @param request
   * @return
   */
  @RequestMapping(value="pay.do",method = RequestMethod.POST)//不能有分号
  @ResponseBody
  public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }
    //拿到path路径
    String path=request.getSession().getServletContext().getRealPath("upload");
    return iOrderService.pay(user.getId(),orderNo,path);
  }

  /**
   * 支付宝回调函数
   * @param request
   * @return
   */
  @RequestMapping(value="alipay_callback",method=RequestMethod.POST)
  @ResponseBody
  public Object alipayCallback(HttpServletRequest request){

    Map<String,String> params= Maps.newHashMap();
    Map requestParams=request.getParameterMap();//

    for(Iterator iter =requestParams.keySet().iterator();iter.hasNext();){
      String name=(String)iter.next();// key

      String[] values=(String[]) requestParams.get(name); //value是个数组

      String valuseStr="";
      for(int i=0;i<values.length;i++){
        if(i==values.length-1){
          valuseStr=valuseStr+values[i];
        }else{
          valuseStr+=values[i]+",";
        }
      }
      params.put(name,valuseStr);
    }

    logger.info("支付宝回调，sign:{},trade_status:{},参数：{}",params.get("sign"),params.get("trade_status"),params);

    //验证回调，是否重复发，避免重复通知
    params.remove("sign_type");

    try {
      boolean AlipayRSACheckedV2= AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());

      if(!AlipayRSACheckedV2){
        return ServerResponse.createByErrorMsg("非法请求，验证不通过");
      }

      //todo  业务逻辑


    } catch (AlipayApiException e) {
      logger.error("支付宝回调异常",e);
      e.printStackTrace();
    }

    //验证数据
    ServerResponse response =iOrderService.alipayCallback(params);

    if(response.isSuccess()){
      return Const.AlipayCallback.RESPONSE_SUCCESS;
    }
    return Const.AlipayCallback.RESPONSE_FAILED;
  }


}
