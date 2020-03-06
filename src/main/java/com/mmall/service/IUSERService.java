package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

import javax.servlet.http.HttpSession;

/**
 * 接口固定格式
 */
public interface IUSERService {
  //????不懂 现在懂了：public abstract 可以省略

  /*
  * 注意：接口中的成员都有固定的修饰符。
	  成员变量：public static final
	  成员方法：public abstract （接口升级：后来添加的方法叫default）
      1.interface Inter{  
      2.    public static final int x = 3;  
      3.    public abstract void show();  void为返回值类型,void是空值 指代下方ServerResponse
      4.}  
  *
  *
  *
  * */

  //redundant 多余的
  public abstract ServerResponse<User> login(String username, String password);

  ServerResponse<String> register(User user);

  ServerResponse<String> checkValid(String str,String type);

  ServerResponse <String> selectQuestion (String username);


  ServerResponse<String> forgetCheckAnswer(String username,String question,String answer);

  ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken);
}
