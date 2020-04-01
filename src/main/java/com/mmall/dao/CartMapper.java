package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdAndProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectIsAllCheckedByUserId(Integer userId);

    int deleteProductByUserId(@Param("userId") Integer userId,@Param("productIds") List<String> productIdList);

    int setCheckedOrUnCheckedProduct(@Param("userId") Integer userId,@Param("checked") Integer checked,@Param("productId") Integer productId);

    int getCartNumByUserId(Integer userId);

    //获取购物车中已经勾选的产品

    List<Cart> selectProductFromCartBySelected(Integer userId);
}