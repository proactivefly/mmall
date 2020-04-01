package com.mmall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVO;
import com.mmall.vo.ProductListItemVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {
  @Autowired
  private ProductMapper productMapper;

  @Autowired
  private CategoryMapper categoryMapper;


  // 评级调用
  @Autowired
  private ICategoryService iCategoryService;

  /**
   * 新增更修改产品信息
   *
   * @param product
   * @return
   */
  public ServerResponse saveOrUpdateProduct(Product product) {
    if (product != null) {
      if (StringUtils.isNotBlank(product.getSubImages())) {
        String[] subImagesArr = product.getSubImages().split(",");
        if (subImagesArr.length > 0) {
          product.setMainImage(subImagesArr[0]);
        }
      }
      if (product.getId() == null) {
        int count = productMapper.insert(product);
        if (count > 0) {
          return ServerResponse.createBySuccessMsg("新增产品成功");
        } else {
          return ServerResponse.createByErrorMsg("新增失败");
        }
      } else {
        int resCount = productMapper.updateByPrimaryKey(product);
        if (resCount > 0) {
          return ServerResponse.createBySuccessMsg("更新产品成功");
        } else {
          return ServerResponse.createByErrorMsg("更新产品失败");
        }
      }
    }
    return ServerResponse.createByErrorMsg("新增或更新产品参数不正确");
  }

  /**
   * 更新销售状态
   *
   * @param productId
   * @param status
   * @return
   */
  public ServerResponse<String> updateProductSaleSataus(Integer productId, Integer status) {
    if (productId == null || status == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    Product product = new Product();
    product.setId(productId);
    product.setStatus(status);
    int result = productMapper.updateByPrimaryKeySelective(product);
    if (result > 0) {
      return ServerResponse.createBySuccessMsg("更改状态成功");
    }
    return ServerResponse.createByErrorMsg("更改状态失败");
  }

  /**
   * 获取商品详情
   *
   * @param productId
   * @return
   */
  public ServerResponse<ProductDetailVO> manageProductDetail(Integer productId) {
    if (productId == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    Product product = productMapper.selectByPrimaryKey(productId);
    if (product == null) {
      return ServerResponse.createByErrorMsg("产品已下架或者删除");
    }
    // VO对象 value Object
    //复杂一点，pojo==>bo(business object)==>vo(view object)
    ProductDetailVO productDetailVo = assembleProductDetailVO(product);
    return ServerResponse.createBySuccess(productDetailVo);
  }

  /**
   * 重新组装前端想要的数据数据
   *
   * @param product
   * @return
   */
  private ProductDetailVO assembleProductDetailVO(Product product) {
    ProductDetailVO productDetailVO = new ProductDetailVO();
    productDetailVO.setCategoryId(product.getCategoryId());
    productDetailVO.setId(product.getId());
    productDetailVO.setDetail(product.getDetail());
    productDetailVO.setMainImage(product.getMainImage());
    productDetailVO.setName(product.getName());
    productDetailVO.setPrice(product.getPrice());
    productDetailVO.setStatus(product.getStatus());
    productDetailVO.setStock(product.getStock());
    productDetailVO.setSubtitle(product.getSubtitle());
    //todo
    productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

    Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
    if (category == null) {
      productDetailVO.setParentCategoryId(0);//默认根节点
    } else {
      productDetailVO.setParentCategoryId(category.getParentId());
    }
    //createTime updateTime
    productDetailVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
    productDetailVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
    return productDetailVO;
  }

  /**
   * 后台获取产品列表
   * @param pageNum
   * @param pageSize
   * @return
   */
  public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
    // 1、startPate start
    // 2、添加sql查询逻辑
    // 3、pageHelper收尾；

    PageHelper.startPage(pageNum, pageSize);
    //原数组
    List<Product> productList = productMapper.selectProductList();
    //重新组装的数据
    List<ProductListItemVO> productListItemVOList = Lists.newArrayList();
    for (Product productItem : productList) {
      ProductListItemVO productListItemVO = assembleProductListItemVO(productItem);
      productListItemVOList.add(productListItemVO);
    }
    //3 收尾
    PageInfo resultPageInfo = new PageInfo(productList);
    resultPageInfo.setList(productListItemVOList);
    return ServerResponse.createBySuccess(resultPageInfo);
  }

  //重新组装列表中每个product数据
  private ProductListItemVO assembleProductListItemVO(Product product) {
    ProductListItemVO ProductListItemVO = new ProductListItemVO();
    ProductListItemVO.setId(product.getId());
    ProductListItemVO.setCategoryId(product.getCategoryId());
    ProductListItemVO.setName(product.getName());
    ProductListItemVO.setPrice(product.getPrice());
    ProductListItemVO.setMainImage(product.getMainImage());
    ProductListItemVO.setSubtitle(product.getSubtitle());
    ProductListItemVO.setStatus(product.getStatus());
    ProductListItemVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
    return ProductListItemVO;
  }

  /**
   * 后台通过产品名称和分类id获取产品列表
   * @param productName
   * @param productId
   * @param pageNum
   * @param pageSize
   * @return
   */
  public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
    PageHelper.startPage(pageNum, pageSize);
    if(StringUtils.isNotBlank(productName)){
      productName = new StringBuilder().append("%").append(productName).append("%").toString();
    }
    List<Product> productList=productMapper.selectProductListByNameOrId(productName,productId);
    List<ProductListItemVO> productListItemVOList=Lists.newArrayList();
    for(Product item :productList){
      ProductListItemVO productListItemVO=assembleProductListItemVO(item);
      productListItemVOList.add(productListItemVO);
    }
    PageInfo pageResult=new PageInfo(productList);
    pageResult.setList(productListItemVOList);

    return ServerResponse.createBySuccess(pageResult);
  }



  /**
   * 前台获取产品详情
   * @param productId
   * @return
   */
  public ServerResponse<ProductDetailVO> userProductDetail(Integer productId) {
    if (productId == null) {
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    Product product = productMapper.selectByPrimaryKey(productId);
    if (product == null) {
      return ServerResponse.createByErrorMsg("产品已下架或者删除");
    }

    //判断销售状态
    if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
      return ServerResponse.createByErrorMsg("产品已下架或者删除");
    }
    // VO对象 value Object
    //复杂一点，pojo==>bo(business object)==>vo(view object)
    ProductDetailVO productDetailVo = assembleProductDetailVO(product);
    return ServerResponse.createBySuccess(productDetailVo);
  }

  public ServerResponse<PageInfo> userSearchProduct(String keyword,Integer categoryId, Integer pageNum,Integer pageSize,String orderBy){
    if(StringUtils.isBlank(keyword) && categoryId==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }

    // 传入大分类  手机属于数码产品
    List<Integer> categoryList=new ArrayList<Integer>();
    if(categoryId!=null){
      Category category=categoryMapper.selectByPrimaryKey(categoryId);
      if(category==null && StringUtils.isBlank(keyword)){
        //没有该分类，并且关键字为空，返回空集合
        PageHelper.startPage(pageNum,pageSize);
        List<ProductListItemVO> productListItemVO=Lists.newArrayList();
        PageInfo pageInfo=new PageInfo(productListItemVO);
        //不需要重新set
        return ServerResponse.createBySuccess(pageInfo);
      }

      categoryList=iCategoryService.selectCategoryAndChildrenCategory(category.getId()).getData();
    }
    if(StringUtils.isNotBlank(keyword)){
      keyword=new StringBuilder().append("%").append(keyword).append("%").toString();
    }
    //排序处理
    PageHelper.startPage(pageNum,pageSize);
    if(StringUtils.isNotBlank(orderBy)){
      if(Const.ProdcutListOrderBy.PRICE_ASE_DESE.contains(orderBy)){
        String[] orderByArr=orderBy.split("_");
        // arg1 按那个字段，arg2升序还是降序
        PageHelper.orderBy(orderByArr[0]+" "+orderByArr[1]);
      }
    }
    List<Product> productList=productMapper.selectProductByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryList.size()==0?null:categoryList);
    List<ProductListItemVO> productListItemVOList=Lists.newArrayList();
    for(Product product : productList){
      ProductListItemVO productListItemVO=assembleProductListItemVO(product);
      productListItemVOList.add(productListItemVO);
    }
    PageInfo pageInfo=new PageInfo(productList);
    pageInfo.setList(productListItemVOList);
    return ServerResponse.createBySuccess(pageInfo);
  }
}
