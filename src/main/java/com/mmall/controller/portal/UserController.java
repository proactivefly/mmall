package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUSERService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
//用户层的接口都放到 user空间下
//RequestMapping是一个用来处理请求地址映射的注解，可用于类或方法上。用于类上，表示类中的所有响应请求的方法都是以该地址作为父路径。
/*
 RequestMapping注解有六个属性，下面分成三类进行说明。

  value： 指定请求的实际地址，指定的地址可以是具体地址、可以RestFul动态获取、也可以使用正则设置；
  method： 指定请求的method类型， 分为GET、POST、PUT、DELETE等；

  consumes： 指定处理请求的提交内容类型（Content-Type），例如application/json, text/html;
  produces: 指定返回的内容类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回；

  params： 指定request中必须包含某些参数值是，才让该方法处理。
  headers：指定request中必须包含某些指定的header值，才能让该方法处理请求。

 */

//用法一 用在类上
@RequestMapping("/user/")


public class UserController {
  @Autowired
  private IUSERService iUserService; //注入 service  此处IUSERService为 public int x=1

  /**
   * 功能描述: <br>
   * <登录功能>
   * @Param:
   * @Return: 
   * @Author: ecsta
   * @Date: 2020/1/9 15:52
   */
  // 配置接口，添加注解，value,method指定为post请求
  //value： 指定请求的实际地址，指定的地址可以是具体地址、可以RestFul动态获取、也可以使用正则设置；
  //method： 指定请求的method类型， 分为GET、POST、PUT、DELETE等；
  @RequestMapping(value="login.do",method= RequestMethod.POST)

  // 将返回值自动进入插件从而序列化json 数据
  @ResponseBody
  //ServerResponse<User>为返回值类型，返回的是接口数据
  public ServerResponse<User> login(String username, String password, HttpSession session){
    //service-->mybatis-->dao
    ServerResponse<User> response=iUserService.login(username,password);
    if(response.isSuccess()){ //如果成功，放入session
      session.setAttribute(Const.CURRENT_USER,response.getData());
    }
    return response;
  }

//————————————————————————————————————————————————————————————————————————————————————————————————————————

  /**
   * 功能描述: <br>
   * <登出接口>
   * @Param: [username, password, session]
   * @Return: com.maven.common.ServerResponse
   * @Author: ecsta
   * @Date: 2020/1/10 17:27
   */
  @RequestMapping(value="logout.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> logout(HttpSession session){
    session.removeAttribute(Const.CURRENT_USER); //移除session中的 用户信息
    return ServerResponse.createBySuccess();
  }

  /**
   * 注册接口
   * @param user
   * @return
   */
  @RequestMapping(value="register.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> register (User user){
    return iUserService.register(user);
  }

  /**
   * 校验用户名邮箱
   * @param str
   * @param type
   * @return
   */
  @RequestMapping(value="check_valid.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> checkValid(String str,String type){
    return iUserService.checkValid(str,type);
  }


  /**
   * 获取用户信息(直接从session拿，无需请求接口)
   * @param session
   * @return
   */

  @RequestMapping(value="get_user_info.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> getUserInfo(HttpSession session){
    User user=(User) session.getAttribute(Const.CURRENT_USER); //强转称User 对象
    if(user!=null){
      return ServerResponse.createBySuccess(user);
    }
    return ServerResponse.createBySuccessMsg("用户未登录，无法获取用户信息");
  }


  /**
   * 忘记问题
   * @param username
   * @return
   */

  @RequestMapping(value="forget_get_question.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> forgetGetQuestion(String username){
    return iUserService.selectQuestion(username);
  }

  /**
   * 判断答案是否正确
   * @param username
   * @param question
   * @param answer
   * @return
   */
  @RequestMapping(value="forget_check_answer.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> forgerCheckAnswer(String username,String question,String answer){
    return iUserService.forgetCheckAnswer(username,question,answer);
  }

  /**
   * 重置密码
   * @param username
   * @param PasswordNew
   * @param forgetToken
   * @return
   */
  @RequestMapping(value="forget_reset_password.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> forgetRestPassword(String username,String PasswordNew,String forgetToken){
    return iUserService.forgetRestPassword(username,PasswordNew,forgetToken);
  }

  /**
   * 更新密码
   * @param session
   * @param password
   * @param passwordNew
   * @return
   */
  @RequestMapping(value="reset_password.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> resetPassword(HttpSession session,String password,String passwordNew){
    //从session中获取user常量，并强转称user对象
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorMsg("请登录");
    }
    return iUserService.resetPassword(user,password,passwordNew);
  }

  /**
   * 更新用户
   * @param session
   * @param user
   * @return
   */
  @RequestMapping(value="update_user_information.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> updateUserInformation(HttpSession session,User user){

    User currentUser=(User)session.getAttribute(Const.CURRENT_USER);
    if(currentUser==null){
      return ServerResponse.createByErrorMsg("请登录后修改您的密码");
    }
    // 把userId设置成当前登录的userId,username也设置称登录用户的user信息
    user.setId(currentUser.getId());
    user.setUsername(currentUser.getUsername());

    //注意这个写法
    ServerResponse<User> response = iUserService.updateUserInformation(user);


    if(response.isSuccess()){
      //更新user告诉前端
      response.getData().setUsername(currentUser.getUsername());
      session.setAttribute(Const.CURRENT_USER,response.getData());
    }
    return response;
  }

  @RequestMapping(value="get_information.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> getUserDetailInfo(HttpSession session){
    //获取当前登录信息
    User currentUser= (User)session.getAttribute(Const.CURRENT_USER);
    if(currentUser==null){
      //强制登录
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    int userId=currentUser.getId();
    return iUserService.getUserInfoDetail(userId);
  }
}
