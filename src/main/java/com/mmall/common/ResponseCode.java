package com.mmall.common;

/**
 * @ClassName ResponseCode
 * @Description TODO  响应编码enmu的枚举类
 * @Author ecsta
 * @Date 2020/1/9 16:19
 * @ModifyDate 2020/1/9 16:19
 * @Version 1.0
 */
public enum ResponseCode {
  SUCCESS(0,"SUCCESS"),
  ERROR(1,"ERROR"),
  NEED_LOGIN(10,"NEED_LOGIN"),
  ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");//参数错误

  /*
   final特点：
    1：这个关键字是一个修饰符，可以修饰类，方法，变量。
    2：被final修饰的类是一个最终类，不可以被继承。
    3：被final修饰的方法是一个最终方法，不可以被覆盖。
    4：被final修饰的变量是一个常量，只能赋值一次。
   */

  private final int code;
  private final String desc;
  //构造器
  ResponseCode(int code ,String desc){
    this.code=code;
    this.desc=desc;
  }
  // 开放参数
  public int getCode(){
    return code;
  }
  public String getDesc(){
    return desc;
  }
}
