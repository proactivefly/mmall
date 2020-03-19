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

  public interface Cart{
    int CEHECK_ON=1;
    int CHECK_OFF=0;
    //限制数量常量
    String LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";
    String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
  }
}
