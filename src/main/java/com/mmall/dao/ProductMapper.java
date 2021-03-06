package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);
    //查询返回的 product信息
    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectProductList();

    List<Product> selectProductListByNameOrId(@Param("productName") String productName,@Param("productId") Integer productId);

    List<Product> selectProductByNameAndCategoryIds(@Param("productName") String productName,@Param("categoryList") List<Integer> categoryList);
}