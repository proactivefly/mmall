package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUSERService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("manage/product")
public class ProductManageController {
  @Autowired
  private IUSERService iUserService;

  @Autowired
  private IProductService iProductService;

  @Autowired
  private IFileService iFileService;
  /**
   * 添加或更新商品
   * @param session
   * @param product
   * @return
   */
  @RequestMapping(value="/save_or_update.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse saveProduct(HttpSession session, Product product){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      return iProductService.saveOrUpdateProduct(product);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  /**
   * 设置产品销售状态
   * @param session
   * @param productId
   * @param status
   * @return
   */
  @RequestMapping(value="/set_sale_status.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      return iProductService.updateProductSaleSataus(productId,status);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  /**
   * 获取商品详细信息
   * @param session
   * @param productId
   * @return
   */
  @RequestMapping(value="/get_detail.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse getDetail(HttpSession session, Integer productId){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      return iProductService.manageProductDetail(productId);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  @RequestMapping(value="/get_product_list.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<PageInfo> getProductList(HttpSession session, @RequestParam(value="pageNum",defaultValue="1") int pageNum,@RequestParam(value="pageSize",defaultValue = "8") int pageSize){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      return iProductService.getProductList(pageNum,pageSize);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  /**通过商品名称或id查询商品列表
   *
   * @param session
   * @param productName
   * @param productId
   * @param pageNum
   * @param pageSize
   * @return
   */
  @RequestMapping(value="/search_product.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<PageInfo> getProductListByNameOrId(HttpSession session, String productName,Integer productId,@RequestParam(value="pageNum",defaultValue="1") int pageNum,@RequestParam(value="pageSize",defaultValue = "8") int pageSize){
    User user =(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }
    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      return iProductService.searchProduct(productName,productId,pageNum,pageSize);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  /**
   * 文件上传
   * @param file
   * @param request
   * file_name对应form表单提交的内容，默认是file
   * @return
   */
  @RequestMapping(value="/upload.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse upload(HttpSession session,@RequestParam(value="file_name",required = false) MultipartFile file, HttpServletRequest request){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    //判断登录
    if(user==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录");
    }

    //判断是否为管理员权限
    if(iUserService.checkAdminRole(user).isSuccess()){
      String path=request.getSession().getServletContext().getRealPath("upload");
      String targetFileName=iFileService.upload(file,path);
      String url= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
      Map fileMap= Maps.newHashMap();
      fileMap.put("uri",targetFileName);
      fileMap.put("url",url);
      return ServerResponse.createBySuccess(fileMap);
    }else{
      return ServerResponse.createByErrorMsg("无权操作，只有管理员可以操作");
    }
  }

  /**
   * 富文本上传
   * @param session
   * @param file
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value="/upload_richText.do",method = RequestMethod.POST)
  @ResponseBody
  public Map richText(HttpSession session, @RequestParam(value="file_name",required = false) MultipartFile file, HttpServletRequest request , HttpServletResponse response){
    User user=(User)session.getAttribute(Const.CURRENT_USER);
    Map returnMap=Maps.newHashMap();
    if(user==null){
      returnMap.put("success",false);
      returnMap.put("msg","请登录");
      return returnMap;
    }
    if(iUserService.checkAdminRole(user).isSuccess()){
      String path=request.getSession().getServletContext().getRealPath("upload");
      String targetFileName=iFileService.upload(file,path);
      if(StringUtils.isBlank(targetFileName)){
        returnMap.put("success",false);
        returnMap.put("msg","上传失败");
        return returnMap;
      }
      String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
      returnMap.put("success",true);
      returnMap.put("msg","上传成功");
      returnMap.put("file_path",url);
      response.addHeader("Access-Control-Allow-Headers","X-File-Name");
      return returnMap;
    }else{
      returnMap.put("success",false);
      returnMap.put("msg","无权操作，只有管理员可以");
      return returnMap;
    }
  }
}
