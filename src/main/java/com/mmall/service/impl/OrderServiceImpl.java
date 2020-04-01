package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private OrderItemMapper orderItemMapper;


  @Autowired

  private CartMapper cartMapper;

  @Autowired
  private PayInfoMapper payInfoMapper;

  @Autowired
  private ProductMapper productMapper;

  @Autowired

  private ShippingMapper shippingMapper;
  private static final Logger log= LoggerFactory.getLogger(OrderServiceImpl.class);

  private static AlipayTradeService tradeService;
  /**
   * 创建订单
   * @param userId
   * @param shippingId
   * @return
   */
  public ServerResponse createOrder(Integer userId,Integer shippingId){

    //todo 从购物车中获取被勾选的商品数据，购物车表中有是多个 备选中的商品，每个商品对应购物车一条记录
    List<Cart> cartList=cartMapper.selectProductFromCartBySelected(userId);
    //todo 计算订单总价
    ServerResponse response=this.getCartOrderItemList(userId,cartList);
    if(!response.isSuccess()){
      return response;
    }
    List<OrderItem> orderItemList=(List<OrderItem>)response.getData();//强转
    BigDecimal payment=this.getOrderTotalPrice(orderItemList);
    //todo 生成订单

    Order order=this.assembleOrder(userId,shippingId,payment);
    if(order == null){
      return ServerResponse.createByErrorMsg("生成订单错误");
    }
    if(CollectionUtils.isEmpty(orderItemList)){
      return ServerResponse.createByErrorMsg("购物车为空");
    }
    //遍历每个order设置 set订单号
    for(OrderItem orderItem : orderItemList){
      orderItem.setOrderNo(order.getOrderNo());
    }
    //mybatis批量插入订单
    orderItemMapper.insertOrderItemAll(orderItemList);

    //生成订单成功，减少库存
    this.reduceProductStock(orderItemList);

    //清空购物车
    this.clearCart(cartList);

    //返回数据给前端
    OrderVo ordetVo =this.assembleOrderVo(order,orderItemList);

    return ServerResponse.createBySuccess(ordetVo);
  }

  /**
   * 取消订单
   * @param userId
   * @param orderNo
   * @return
   */
  public ServerResponse<String> cancel(Integer userId,long orderNo){
    Order order=orderMapper.selectOrderByUIdAndOrderNo(userId,orderNo);
    //判断是否订单
    if(order==null){
      return ServerResponse.createByErrorMsg("该用户这个订单不存在");
    }

    //判断订单状态
    if(order.getStatus()!=Const.OrderStatusEnum.NO_PAY.getCode()){
      return ServerResponse.createByErrorMsg("已付款无法取消订单");
    }

    //更新订单
    Order updateOrder =new Order();
    updateOrder.setId(order.getId());
    updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
    int rowCount=orderMapper.updateByPrimaryKeySelective(updateOrder);
    if(rowCount>0){
      return ServerResponse.createBySuccess();
    }else{
      return ServerResponse.createByError();
    }
  }

  /**
   * 获取购物车中选中de商品列表
   * @param userId
   * @return
   */
  public ServerResponse getCartProductDetail(Integer userId){
    OrderProductVo orderProductVo = new OrderProductVo();
    //从购物车中获取数据
    List<Cart> cartList = cartMapper.selectProductFromCartBySelected(userId);
    ServerResponse serverResponse =  this.getCartOrderItemList(userId,cartList);
    if(!serverResponse.isSuccess()){
      return serverResponse;
    }
    List<OrderItem> orderItemList =( List<OrderItem> ) serverResponse.getData();
    //创建orderItem List
    List<OrderItemVo> orderItemVoList = Lists.newArrayList();
    //初始化总价
    BigDecimal payment = new BigDecimal("0");
    for(OrderItem orderItem : orderItemList){
      payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
      //组装orderItem
      orderItemVoList.add(this.assembleOrderItemVo(orderItem));
    }
    orderProductVo.setProductTotalPrice(payment);
    orderProductVo.setOrderItemVoList(orderItemVoList);
    orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
    return ServerResponse.createBySuccess(orderProductVo);
  }


  /**
   * 获取某一个 订单详情
   * @param userId
   * @param orderNo
   * @return
   */
  public ServerResponse getOrderDetail(Integer userId,long orderNo){
    Order order=orderMapper.selectOrderByUIdAndOrderNo(userId,orderNo);
    if(order!=null){
      List<OrderItem> orderItemList=orderItemMapper.getOrderItemListByUserIdAndOrderNo(userId,orderNo);

      OrderVo orderVo =this.assembleOrderVo(order,orderItemList);

      return ServerResponse.createBySuccess(orderVo);
    }
    return ServerResponse.createByErrorMsg("该用户下无该订单");

  }

  /**
   * 获取订单列表
   * @param userId
   * @return
   *
   * order List 包含多个order
   *    order 包含多个  orderItem，每个orderItem 对应一个product
   *
   *
   *
   *
   *
   *
   *
   */
  public ServerResponse<PageInfo> getList(Integer userId, Integer pageNum,Integer pageSize){
    PageHelper.startPage(pageNum,pageSize);

    List<Order> orderList=orderMapper.getListByUserId(userId);
    List<OrderVo> orderVoList=this.assembleOrderVoList(orderList,userId);

    PageInfo pageInfo =new PageInfo(orderList);

    pageInfo.setList(orderVoList);

    return ServerResponse.createBySuccess(pageInfo);
  }

  // todo 组装orderVoList !!!!!多个订单的VO

  private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
    List<OrderVo> orderVoList=Lists.newArrayList();
    for(Order order :orderList){
      List<OrderItem> orderItemList=Lists.newArrayList(); //每个order 的orderItem集合

      if(userId==null){ //管理员身份不需要要userId
        orderItemList=orderItemMapper.getOrderItemListByOrderNo(order.getOrderNo());
      }else{
        orderItemList=orderItemMapper.getOrderItemListByUserIdAndOrderNo(userId,order.getOrderNo());
      }

      OrderVo orderVo =this.assembleOrderVo(order,orderItemList);

      orderVoList.add(orderVo);

    }

    return orderVoList;


  }
  //  todo 组装orderVo !!!!!单个订单VO
  private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
    OrderVo orderVo = new OrderVo();
    orderVo.setOrderNo(order.getOrderNo());
    orderVo.setPayment(order.getPayment());
    orderVo.setPaymentType(order.getPaymentType());
    orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

    orderVo.setPostage(order.getPostage());
    orderVo.setStatus(order.getStatus());
    orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

    orderVo.setShippingId(order.getShippingId());
    Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
    if(shipping != null){
      orderVo.setReceiverName(shipping.getReceiverName());
      orderVo.setShippingVo(assembleShippingVo(shipping));
    }

    orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
    orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
    orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
    orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
    orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));


    orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


    List<OrderItemVo> orderItemVoList = Lists.newArrayList();

    for(OrderItem orderItem : orderItemList){
      OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
      orderItemVoList.add(orderItemVo);
    }
    orderVo.setOrderItemVoList(orderItemVoList);
    return orderVo;
  }

  //todo 组装orderItemVo
  private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
    OrderItemVo orderItemVo = new OrderItemVo();
    orderItemVo.setOrderNo(orderItem.getOrderNo());
    orderItemVo.setProductId(orderItem.getProductId());
    orderItemVo.setProductName(orderItem.getProductName());
    orderItemVo.setProductImage(orderItem.getProductImage());
    orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
    orderItemVo.setQuantity(orderItem.getQuantity());
    orderItemVo.setTotalPrice(orderItem.getTotalPrice());

    orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
    return orderItemVo;
  }

  //todo 组装 收货地址vo
  private ShippingVo assembleShippingVo(Shipping shipping){
    ShippingVo shippingVo = new ShippingVo();
    shippingVo.setReceiverName(shipping.getReceiverName());
    shippingVo.setReceiverAddress(shipping.getReceiverAddress());
    shippingVo.setReceiverProvince(shipping.getReceiverProvince());
    shippingVo.setReceiverCity(shipping.getReceiverCity());
    shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
    shippingVo.setReceiverMobile(shipping.getReceiverMobile());
    shippingVo.setReceiverZip(shipping.getReceiverZip());
    shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
    return shippingVo;
  }
  //todo 清空购物车
  private void clearCart(List<Cart> cartList){
    for(Cart cartItem :cartList){
      cartMapper.deleteByPrimaryKey(cartItem.getId());
    }
  }
  //todo 组装订单信息
  private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal totalPrice){
    //生成订单号
    Order order=new Order();
    long orderNo=this.generateOrderNo();
    order.setOrderNo(orderNo);
    order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
    order.setPostage(0);
    order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
    order.setPayment(totalPrice);

    order.setUserId(userId);
    order.setShippingId(shippingId);
    //发货时间等等
    //付款时间等等
    int rowCount = orderMapper.insert(order);
    if(rowCount > 0){
      return order;
    }
    return null;
  }
  //todo 减少库存
  private void reduceProductStock(List<OrderItem> orderItemList){
    for(OrderItem orderItem :orderItemList){
      Product product=productMapper.selectByPrimaryKey(orderItem.getProductId());
      product.setStock(product.getStock()-orderItem.getQuantity());
      productMapper.updateByPrimaryKeySelective(product);
    }
  }
  //todo 生成订单号
  private long generateOrderNo(){
    long currentTime=System.currentTimeMillis();

    return currentTime+new Random().nextInt(100);
  }
  //todo 计算总价
  private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
    BigDecimal payment=new BigDecimal("0");

    for(OrderItem orderItem :orderItemList){
      payment=BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
    }

    return payment;
  }
  //todo 获取订单item
  private ServerResponse getCartOrderItemList(Integer userId,List<Cart> cartList){

    if(CollectionUtils.isEmpty(cartList)){
      return ServerResponse.createBySuccessMsg("购物车为空");
    }

    //todo 校验购物车数据，产品数量和转态
    List<OrderItem> orderItemList=Lists.newArrayList();

    for(Cart cartItem :cartList){
      //售卖状态
      Product productItem=productMapper.selectByPrimaryKey(cartItem.getProductId());
      if(Const.ProductStatusEnum.ON_SALE.getCode()!=productItem.getStatus()){
        return ServerResponse.createByErrorMsg("当前商品:"+productItem.getName()+"已下架");
      }
      //库存
      if(cartItem.getQuantity()>productItem.getStock()){
        return ServerResponse.createByErrorMsg("商品:"+productItem.getName()+"库存不足");
      }

      //组装数据
      OrderItem orderItem =new OrderItem();
      orderItem.setUserId(userId);
      orderItem.setProductId(productItem.getId());
      orderItem.setProductName(productItem.getName());
      orderItem.setProductImage(productItem.getMainImage());
      //价格快照
      orderItem.setCurrentUnitPrice(productItem.getPrice());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setTotalPrice(BigDecimalUtil.mul(productItem.getPrice().doubleValue(),cartItem.getQuantity()));
      orderItemList.add(orderItem);
    }
    return ServerResponse.createBySuccess(orderItemList);
  }
  // 简单打印应答
  private void dumpResponse(AlipayResponse response) {
    if (response != null) {
      log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
      if (StringUtils.isNotEmpty(response.getSubCode())) {
        log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
            response.getSubMsg()));
      }
      log.info("body:" + response.getBody());
    }
  }

  /**
   * 支付
   * @param userId
   * @param orderNo
   * @param path
   * @return
   */
  public ServerResponse pay(Integer userId , Long orderNo, String path){
    //创建一个map，不封装vo对象了
    /**
     * Maps.newHashMap() 和 new HashMap()一样
     */
    Map<String,String> resultMap= Maps.newHashMap();
//    Map<String,String> resultMap1=new HashMap<>();
    Order order = orderMapper.selectOrderByUIdAndOrderNo(userId,orderNo);
    if(order==null){
      return ServerResponse.createByErrorMsg("用户没有该订单");
    }
    //转成orderNo 转成
    resultMap.put("orderNo" , String.valueOf(order.getOrderNo()));
    //组装生成支付宝订单的参数

    // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
    // 需保证商户系统端不能重复，建议通过数据库sequence生成，
    String outTradeNo = order.getOrderNo().toString();

    /**
     * StringBuilder() 快于 buffer()
     *
     * StringBuffer()
     *
     * 字符串拼接(太 low)   其实底层 通过 StringBugger()实现
     */

    // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
    String subject = new StringBuilder().append("快乐moon商城，订单号是：").append(outTradeNo).toString();

//    String subject ="快乐木商城" + outTradeNo;

    // (必填) 订单总金额，单位为元，不能超过1亿元
    // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
    String totalAmount = order.getPayment().toString();

    // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
    // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
    String undiscountableAmount = "0";

    // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
    // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
    String sellerId = "";

    // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
    String body = new StringBuilder().append("订单：").append(outTradeNo).append("共计：").append(totalAmount).toString();

    // 商户操作员编号，添加此参数可以为商户操作员做销售统计
    String operatorId = "test_operator_id";

    // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
    String storeId = "test_store_id";

    // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
    ExtendParams extendParams = new ExtendParams();
    extendParams.setSysServiceProviderId("2088100200300400500");

    // 支付超时，定义为120分钟
    String timeoutExpress = "120m";

    // 商品明细列表，需填写购买商品详细信息，
    List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();


    //订单itme集合
    List<OrderItem> orderItemList=orderItemMapper.getOrderItemListByUserIdAndOrderNo(userId,orderNo);

    for(OrderItem orderItem : orderItemList){

      // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
      //GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);

      GoodsDetail goods = GoodsDetail.newInstance(
          orderItem.getProductId().toString(),
          orderItem.getProductName(),
          BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
          orderItem.getQuantity()
      );

      // BigDecimalUtil.mul 接收的事double类型
      goodsDetailList.add(goods);
    }

    // 创建扫码支付请求builder，设置请求参数
    AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
        .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
        .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
        .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
        .setTimeoutExpress(timeoutExpress)
        .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
        .setGoodsDetailList(goodsDetailList);



    /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
     *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
     */
    Configs.init("zfbinfo.properties");

    /** 使用Configs提供的默认参数
     *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
     */
    tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

    AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
    switch (result.getTradeStatus()) {
      case SUCCESS:
        log.info("支付宝预下单成功: )");

        AlipayTradePrecreateResponse response = result.getResponse();
        dumpResponse(response);
        //创建upload文件夹
        File folder=new File(path);

        if(!folder.exists()){
          folder.setWritable(true);
          folder.mkdirs();
        }
        // 需要修改为运行机器上的路径
        String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo()); //图片路径

        String qrFileName=String.format("qr-%s.png",response.getOutTradeNo());//普片文件名，订单号替换%s


        ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

        File targetFile=new File(path,qrFileName);

        try {
          FTPUtil.uploadFile(Lists.newArrayList(targetFile));
        } catch (IOException e) {
          log.info("上传异常"+e);
        }

        log.info("filePath:" + qrPath);

        String qrUrl=PropertiesUtil.getProperty("ftp.server.http.prefix")+qrFileName;

        resultMap.put("quUrl",qrUrl);

        return ServerResponse.createBySuccess(resultMap);
      case FAILED:
        log.error("支付宝预下单失败!!!");

        return ServerResponse.createByErrorMsg("支付宝预下单失败!!!");
      case UNKNOWN:
        log.error("系统异常，预下单状态未知!!!");
        return ServerResponse.createByErrorMsg("系统异常，预下单状态未知!!!");


      default:
        log.error("不支持的交易状态，交易返回异常!!!");

        return ServerResponse.createByErrorMsg("不支持的交易状态，交易返回异常!!!");
    }
  }

  /**
   * 支付宝回调
   * @param params
   * @return
   */
  public ServerResponse alipayCallback(Map<String,String> params){
    Long orderNo=Long.parseLong(params.get("out_trade_no"));//?

    String tradeNo=params.get("trade_no");

    String tradeStatus=params.get("trade_status");

    Order order=orderMapper.selectOrderByOrderNo(orderNo);

    if(order==null){
      return ServerResponse.createByErrorMsg("非商城订单忽略");
    }
    if(order.getStatus()>= Const.OrderStatusEnum.PAYED.getCode()){
      return ServerResponse.createBySuccessMsg("支付宝重复调用");
    }

    //调用成功 ,订单状态改为已付款

    if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
      order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
      order.setStatus(Const.OrderStatusEnum.PAYED.getCode());
      orderMapper.updateByPrimaryKeySelective(order);
    }

    //更新数据库
    orderMapper.updateByPrimaryKeySelective(order);

    PayInfo payInfo=new PayInfo();
    payInfo.setUserId(order.getUserId());
    payInfo.setOrderNo(order.getOrderNo());
    payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
    payInfo.setPlatformStatus(tradeStatus);

    payInfoMapper.insert(payInfo);

    return ServerResponse.createBySuccess(payInfo);
  }


  //backend-----------------------------------------------------------------------------------------------

  /**
   * 后台查看所有订单列表
   * @param pageNum
   * @param pageSize
   * @return
   */
  public ServerResponse<PageInfo> manageList(int pageNum,int pageSize){
    PageHelper.startPage(pageNum,pageSize);
    List<Order> orderList = orderMapper.selectAllOrder();
    //组装 orderVoList对象
    List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
    PageInfo pageResult = new PageInfo(orderList);
    pageResult.setList(orderVoList);
    return ServerResponse.createBySuccess(pageResult);
  }

  /**
   * 查看订单详情
   * @param orderNo
   * @return
   */
  public ServerResponse<OrderVo> manageDetail(Long orderNo){
    Order order = orderMapper.selectOrderByOrderNo(orderNo);
    if(order != null){
      List<OrderItem> orderItemList = orderItemMapper.getOrderItemListByOrderNo(orderNo);
      OrderVo orderVo = assembleOrderVo(order,orderItemList);
      return ServerResponse.createBySuccess(orderVo);
    }
    return ServerResponse.createByErrorMsg("订单不存在");
  }


  /**
   * 订单号精准查询
   * @param orderNo
   * @param pageNum
   * @param pageSize
   * @return
   */
  public ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize){
    PageHelper.startPage(pageNum,pageSize);
    Order order = orderMapper.selectOrderByOrderNo(orderNo);
    if(order != null){
      List<OrderItem> orderItemList = orderItemMapper.getOrderItemListByOrderNo(orderNo);
      OrderVo orderVo = assembleOrderVo(order,orderItemList);

      PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
      pageResult.setList(Lists.newArrayList(orderVo));
      return ServerResponse.createBySuccess(pageResult);
    }
    return ServerResponse.createByErrorMsg("订单不存在");
  }

  /**
   * 订单发货
   * @param orderNo
   * @return
   */
  public ServerResponse<String> manageSendGoods(Long orderNo){
    Order order= orderMapper.selectOrderByOrderNo(orderNo);
    if(order != null){
      if(order.getStatus() == Const.OrderStatusEnum.PAYED.getCode()){
        order.setStatus(Const.OrderStatusEnum.SEND.getCode());
        order.setSendTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        return ServerResponse.createBySuccess("发货成功");
      }
    }
    return ServerResponse.createByErrorMsg("订单不存在");
  }




}
