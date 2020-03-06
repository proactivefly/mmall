package com.mmall.common;

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
}
