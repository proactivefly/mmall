package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVO;
import org.springframework.web.bind.annotation.RequestParam;

public interface IProductService {
  ServerResponse saveOrUpdateProduct(Product product);
  ServerResponse updateProductSaleSataus(Integer productId,Integer status);
  ServerResponse<ProductDetailVO> manageProductDetail(Integer productId);
  ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
  ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);

  //前台客户端 接口
  ServerResponse<ProductDetailVO> userProductDetail(Integer productId);
  ServerResponse<PageInfo> userSearchProduct(String keyword,Integer categoryId, Integer pageNum,Integer pageSize,String orderBy);
}
