package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @ClassName ServerResponse
 * @Description TODO
 * @Author ecsta
 * @Date 2020/1/9 16:04
 * @ModifyDate 2020/1/9 16:04
 * @Version 1.0
 */
//---------------------对结果进行处理，如果data是null 则不返回data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)

//<T> 泛型 代表数据类型，
// 实现序列化接 口  Serializable
public class ServerResponse<T> implements Serializable {
  private int status;//  状态码
  private String msg;// 返回值msg
  private T data;// 返回值 泛型

 //私有构造器 构造方法全部变成t
  private ServerResponse(int status){
    this.status=status;
  }
  //第二个参数非string的时候会调 泛型的构造器
  private ServerResponse(int status, T data){
    this.status=status;
    this.data=data;
  }
  private ServerResponse(int status, String msg, T data){
    this.status=status;
    this.msg=msg;
    this.data=data;
  }

  private ServerResponse(int status, String msg){
    this.status=status;
    this.msg=msg;
  }

  //确认是否成功的响应  (JsonIgnore 祛除序列化里的isSuccess字段)
  @JsonIgnore

  public boolean isSuccess(){ //对应 common 下 ResponseCode 文件
    return this.status==ResponseCode.SUCCESS.getCode();
  }


  public int getStatus(){
    return status;
  }

  public T getData(){
    return data;
  }

  public String getMsg(){
    return msg;
  }

  /**
   * 对外暴露方法
   * @param
   * @return
   */



  //对外暴露成功的响应
  public static <T> ServerResponse<T> createBySuccess(){
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
  }

  public static <T> ServerResponse<T> createBySuccessMsg(String msg){
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
  }

  //-------------------------------------------------
  public static <T> ServerResponse<T> createBySuccess(T data){
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
  }

  public static <T> ServerResponse<T> createBySuccess(String msg, T data){
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
  }
  //--------------------------------------------------------

  //错误的  对外暴露
  public static <T> ServerResponse<T> createByError(){
    return new ServerResponse<T>(ResponseCode.ERROR.getCode());
  }

  public static <T> ServerResponse<T> createByErrorMsg(String msg){
    return new ServerResponse<T>(ResponseCode.ERROR.getCode(),msg);
  }

  // 暴露可变的code,因为是可变的所以code需要单独传
  public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMsg){
    return new ServerResponse<T>(errorCode,errorMsg);
  }
}
