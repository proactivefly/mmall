package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @ClassName Const
 * @Description TODO
 * @Author ecsta
 * @Date 2020/1/10 17:11
 * @ModifyDate 2020/1/10 17:11
 * @Version 1.0
 */
public class Const {
  public static final String CURRENT_USER="currentUser";

  public static final String EMAIL="email";

  public static final String USERNAME="username";

  // 通过内部类 进行分组
  public interface Role{
    int ROLE_CUSTOMER=0;//普通用户
    int ROLE_ADMIN=1;//管理员
  }

  public interface ProdcutListOrderBy{
    //为什么用set ，因为set 的contain方法效率高
    Set<String> PRICE_ASE_DESE= Sets.newHashSet("price_desc","price_asc");
  }

  public enum ProductStatusEnum{
    ON_SALE(1,"在线");
    private String value;
    private int code;

    ProductStatusEnum(int code ,String value){
      this.value=value;
      this.code=code;
    }

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }
  }

  public interface Cart{ //不需要有描述，就用接口
    int CHECK_ON=1;
    int CHECK_OFF=0;
    //限制数量常量
    String LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";
    String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
  }

  public enum OrderStatusEnum{
    CANCELED(0,"已取消"),

    NO_PAY(10,"未支付"),

    PAYED(20,"已付款"),

    SEND(30,"已发货"),

    FINISH(40,"订单完成"),

    CLOSE(50,"订单关闭");

    private int code;
    private String value;

    OrderStatusEnum(int code ,String value){
      this.code=code;
      this.value=value;
    }

    public String getValue(){
      return value;
    }

    public int getCode(){
      return code;
    }

    public static OrderStatusEnum codeOf(int code){
      for(OrderStatusEnum OrderStatusEnum : values()){
        if(OrderStatusEnum.getCode() == code){
          return OrderStatusEnum;
        }
      }
      throw new RuntimeException("么有找到对应的枚举");
    }
  }

  public interface  AlipayCallback{
    String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
    String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

    String RESPONSE_SUCCESS = "success";
    String RESPONSE_FAILED = "failed";
  }



  public enum PayPlatformEnum{
    ALIPAY(1,"支付宝");

    PayPlatformEnum(int code,String value){
      this.code = code;
      this.value = value;
    }
    private String value;
    private int code;

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }
  }

  public enum PaymentTypeEnum{
    ONLINE_PAY(1,"在线支付");

    PaymentTypeEnum(int code,String value){
      this.code = code;
      this.value = value;
    }
    private String value;
    private int code;

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }


    public static PaymentTypeEnum codeOf(int code){
      for(PaymentTypeEnum paymentTypeEnum : values()){
        if(paymentTypeEnum.getCode() == code){
          return paymentTypeEnum;
        }
      }
      throw new RuntimeException("么有找到对应的枚举");
    }

  }

}
