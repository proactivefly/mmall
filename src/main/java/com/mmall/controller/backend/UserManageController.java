package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUSERService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user/")

public class UserManageController {
  @Autowired
  private IUSERService iuserService;

  @RequestMapping(value="login.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> login(String username, String password, HttpSession session){
    ServerResponse<User> response =iuserService.login(username,password);
    if(response.isSuccess()){
      User user=response.getData();
      if(user.getRole()== Const.Role.ROLE_ADMIN){
//        user.setPassword(StringUtils.EMPTY);
        session.setAttribute(Const.CURRENT_USER,user);
        return response;
      }
      return ServerResponse.createByErrorMsg("不是管理员无法登陆");
    }

    return response;
  }
  @RequestMapping(value="get_deep_category.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session , @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){

    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"不登录你就想获取数据，你想什么呢");
    }
    if(iuserService.checkAdminRole(user).isSuccess()){//是否为管理员
      //todo递归子节点
//      return iCategoryService.selectCategoryAndChildrenCategory(categoryId);\
      return null;
    }else{

      return ServerResponse.createByErrorMsg("无操作权限，只有管理员可以修改");
    }
  }
}
