package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUSERService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author ecsta
 * @Date 2020/1/9 16:02
 * @ModifyDate 2020/1/9 16:02
 * @Version 1.0
 */
@Service("iUserService")//注入controller
public class UserServiceImpl implements IUSERService {

  @Autowired //注解
  private UserMapper userMapper; //引入数据库

  @Override //重写接口中的login方法
  public ServerResponse<User> login(String username, String password) {
    // 调用检查是否存在
    int resultCount=userMapper.checkUsername(username);
    if(resultCount==0){
      return ServerResponse.createByErrorMsg("用户不存在！");//服务端响应类
    }
    // todo 密码登录MD5加密
    String MD5Password=MD5Util.MD5EncodeUtf8(password);
    User user=userMapper.selectLogin(username,MD5Password);
    if(user==null){
      return ServerResponse.createByErrorMsg("密码错误！");
    }
    // 把密码置空??
    user.setPassword(StringUtils.EMPTY);
    return ServerResponse.createBySuccessMsg("登录成功");
  }

  //注册接口
  public ServerResponse<String> register(User user){

    ServerResponse validResponse=this.checkValid(user.getUsername(),Const.CURRENT_USER);
    if(!validResponse.isSuccess()){
//      return ServerResponse.createByErrorMsg("用户名已存在");
      return validResponse;
    }

    validResponse=this.checkValid(user.getEmail(),Const.EMAIL);

    if(!validResponse.isSuccess()){

//      return ServerResponse.createBySuccessMsg("邮箱已存在");
      return validResponse;
    }

    user.setRole(Const.Role.ROLE_CUSTOMER);//设置默认角色

    // todo 密码登录MD5加密
    user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
    int resultCount=userMapper.insert(user);//插入数据库
    if(resultCount==0){
      return ServerResponse.createByErrorMsg("注册失败");
    }

    return ServerResponse.createBySuccessMsg("注册成功");
  }

  /**
   * 校验 用户名和邮箱是否可用 true 不存在，false 已存在
   * @param str
   * @param type
   * @return
   */
  public ServerResponse<String> checkValid (String str,String type) {
    if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
      //校验
      if (Const.USERNAME.equals(type)) {
        int resultCount = userMapper.checkUsername(str);
        if (resultCount > 0) {
          return ServerResponse.createByErrorMsg("用户名已存在");
        }
      }

      if (Const.EMAIL.equals(type)) {//如果是邮箱
        int count = userMapper.checkEmail(str);
        if (count > 0) {
          return ServerResponse.createByErrorMsg("邮箱已存在");
        }
      }
    } else {
      return ServerResponse.createByErrorMsg("参数错误");
    }
    return ServerResponse.createBySuccessMsg("校验成功");
  }


  public ServerResponse<String> selectQuestion (String username){
//    int resultCount =userMapper.checkUsername();
      ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
      if(validResponse.isSuccess()){
        return ServerResponse.createByErrorMsg("用户名不存在");
      }
      String question=userMapper.selectQuestionByUsername(username);
      if(org.apache.commons.lang3.StringUtils.isNotBlank(question)){
        return ServerResponse.createBySuccess(question);
      }
      return ServerResponse.createByErrorMsg("找回密码的问题是空的");
  }

  public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
    int resultCount =userMapper.checkAnswer(username,question,answer);
    if(resultCount>0){
      // 说明问题答案正确
//      return ServerResponse.createBySuccessMsg("")
      String forgetToken = UUID.randomUUID().toString();
      TokenCache.setKey(TokenCache.Token_PREFIX+username,forgetToken);
      return ServerResponse.createBySuccess(forgetToken);
    }

    return ServerResponse.createByErrorMsg("问题答案错误");
  }

  public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
    if(org.apache.commons.lang3.StringUtils.isNotBlank(forgetToken)){
      return ServerResponse.createByErrorMsg("参数错误，token需要传递");
    }
    ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
    if(validResponse.isSuccess()){
      return ServerResponse.createByErrorMsg("用户不存在");
    }

    String token = TokenCache.getKey(TokenCache.Token_PREFIX+username);
    if(org.apache.commons.lang3.StringUtils.isNotBlank(token)){
      return ServerResponse.createByErrorMsg("token无效或者过期");
    }

    if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
      String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
      int rowCount=userMapper.updatePasswordByUsername(username,md5Password);
      if(rowCount>0){
        return ServerResponse.createBySuccessMsg("修改密码成功");
      }
    }else{
      return ServerResponse.createByErrorMsg("请重新获取token");
    }
    return ServerResponse.createByErrorMsg("修改密码失败");
  };

  public ServerResponse<String> resetPassword(User user,String password,String passwordNew){
    //避免横向越权,校验用户旧密码,查询count 需要制定id
    int count =userMapper.checkPassword(MD5Util.MD5EncodeUtf8(password),user.getId());
    if(count==0){
      return ServerResponse.createByErrorMsg("旧密码错误");
    }

    user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
    //有变化就更新，没有就不更新，不是全量更新
    count = userMapper.updateByPrimaryKeySelective(user);
    if(count>0){
      return ServerResponse.createBySuccessMsg("更新密码成功");
    }

    return ServerResponse.createByErrorMsg("更新密码失败");
  }


  public ServerResponse<User> updateUserInformation(User user){
    //用户名的更新

    //邮箱更新校验是否重复
    int count =userMapper.checkEmailByUserId(user.getEmail(),user.getId());
    if(count>0){
      return ServerResponse.createByErrorMsg("邮箱已存在,请更换email");
    }

    User newUserInfo=new User();
    newUserInfo.setId(user.getId());
    newUserInfo.setEmail(user.getEmail());
    newUserInfo.setPhone(user.getPhone());
    newUserInfo.setQuestion(user.getQuestion());
    newUserInfo.setAnswer(user.getAnswer());

    int resultCount=userMapper.updateByPrimaryKeySelective(newUserInfo);
    if(resultCount>0){
      return ServerResponse.createBySuccess("更新成功",newUserInfo);
    }
    return ServerResponse.createByErrorMsg("更新个人信息失败");
  }


  public ServerResponse<User> getUserInfoDetail(Integer userId){
    //通过userId查询用户对象
    User currentUser=userMapper.selectByPrimaryKey(userId);
    if(currentUser==null){
      return ServerResponse.createByErrorMsg("找不到该用户");
    }
    //如果正确密码置空 为啥?因为密码字段不能不暴露给前端
    currentUser.setPassword(StringUtils.EMPTY);
    return ServerResponse.createBySuccess(currentUser);
  }
}
