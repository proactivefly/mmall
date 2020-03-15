package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUSERService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")

public class CategoryManageController {
  //注解
  @Autowired
  private IUSERService iUserService;
  @Autowired
  private ICategoryService iCategoryService;


  /**
   * 添加类目
   * @param session
   * @param categoryName
   * @param parentId
   * @return
   */
  @RequestMapping(value="add_category.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam (value="parentId", defaultValue="0") int parentId){
    //判断登录
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
    }
    //校验是否为管理员
    if(iUserService.checkAdminRole(user).isSuccess()){
      //todo
      return iCategoryService.addCategory(categoryName,parentId);
    }else{
      return ServerResponse.createByErrorMsg("无权限操作，需要管理员权限");
    }
  }

  /**
   * 修改 品类拼字
   * @param session
   * @param categoryId
   * @param categoryName
   * @return
   */
  @RequestMapping(value="set_category_name.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse setCategoryName(HttpSession session , Integer categoryId ,String categoryName){
    //判断登录
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
    }
    //校验是否为管理员
    if(iUserService.checkAdminRole(user).isSuccess()){
      //todo
      return iCategoryService.setCategory(categoryName,categoryId);
    }else{
      return ServerResponse.createByErrorMsg("无权限操作,只有管理员可以修改");
    }
  }

  /**
   * 获取第一级子节点的分类
   * @param session
   * @param categoryId
   * @return
   */
  @RequestMapping(value="get_category.do")
  @ResponseBody
  public ServerResponse getChildrenParallelCategory(HttpSession session , @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){

    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"不登录你就想获取数据，你想什么呢");
    }
    if(iUserService.checkAdminRole(user).isSuccess()){//是否为管理员
      return iCategoryService.getChildrenParallelCategory(categoryId);
    }else{
      return ServerResponse.createByErrorMsg("无操作权限，只有管理员可以修改");
    }
  }

  /**
   * 深度查询类目明
   * @param session
   * @param categoryId
   * @return
   */
  @RequestMapping(value="get_deep_category.do",method= RequestMethod.POST)
  @ResponseBody
  public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session , @RequestParam(value="categoryId",defaultValue = "0") Integer categoryId){

    User user=(User)session.getAttribute(Const.CURRENT_USER);
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"不登录你就想获取数据，你想什么呢");
    }
    if(iUserService.checkAdminRole(user).isSuccess()){//是否为管理员
      //todo递归子节点
      return iCategoryService.selectCategoryAndChildrenCategory(categoryId);
    }else{
      return ServerResponse.createByErrorMsg("无操作权限，只有管理员可以修改");
    }
  }
}
