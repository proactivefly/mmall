package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product/")
public class ProductController {
  @Autowired
  private IProductService iProductService;


  /**
   * 前台用户获取商品详情
   * @param productId
   * @return
   */
  @RequestMapping(value="product_detail.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<ProductDetailVO> getDetail(Integer productId){
    return iProductService.userProductDetail(productId);
  }


  /**
   * 前台用户关键字查找
   * @param keyword
   * @param categoryId
   * @param pageNum
   * @param pageSize
   * @param orderBy
   * @return
   */
  @RequestMapping(value="user_search_product.do",method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<PageInfo> getList(@RequestParam(value="keyword",required = false) String keyword,
                                          @RequestParam(value="categoryId",required = false) Integer categoryId,
                                          @RequestParam(value="pageNum",defaultValue = "1") Integer pageNum,
                                          @RequestParam(value="pageSize",defaultValue = "10") Integer pageSize,
                                          @RequestParam(value="orderBy",defaultValue = "") String orderBy){

    return iProductService.userSearchProduct(keyword,categoryId,pageNum,pageSize,orderBy);
  }
}
