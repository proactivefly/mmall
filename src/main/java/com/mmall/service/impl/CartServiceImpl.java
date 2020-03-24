package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartItemListVo;
import com.mmall.vo.CartItemVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
  @Autowired
  private CartMapper cartMapper;

  @Autowired
  private ProductMapper productMapper;


  public ServerResponse<CartItemListVo> add(Integer userId,Integer productId,Integer count){
    if(productId==null || count==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }

    Cart cart= cartMapper.selectCartByUserIdAndProductId(userId,productId);
    if(cart==null){
      //产品不再购物车，需要新增
      Cart cartItem=new Cart();
      cartItem.setQuantity(count);
      cartItem.setProductId(productId);
      cartItem.setUserId(userId);
      cartItem.setChecked(Const.Cart.CHECK_ON);
      //插入记录
      int rowCount =cartMapper.insertSelective(cartItem);
      if(rowCount>0){
        return ServerResponse.createBySuccessMsg("添加购物车成功");
      }else{
        return ServerResponse.createByErrorMsg("添加购物车失败");
      }
    }else{ //产品已存在
      cart.setQuantity(cart.getQuantity()+count);
      cartMapper.updateByPrimaryKeySelective(cart);
    }
    CartItemListVo cartItemListVo=this.getCartItemListVOLimit(userId);
    return ServerResponse.createBySuccess(cartItemListVo);
  }

  /**
   * 返回被限制的购物车 情况
   * @param userId
   * @return
   */
  private CartItemListVo getCartItemListVOLimit(Integer userId){
    //购物车VO
    CartItemListVo cartItemListVo=new CartItemListVo();
    // pojo对象
    List<Cart> cartListOrigin=cartMapper.selectCartByUserId(userId);

    //单个 购物车种单个商品的集合(参数为单个商品VO)
    List<CartItemVo> cartItemVoList= Lists.newArrayList();

    //初始化购物车总价
    //商业计算中一定要用String构造器
    BigDecimal totalPrice=new BigDecimal("0");//使用string构造器，避免丢失精度

    if(CollectionUtils.isNotEmpty(cartListOrigin)){
      for(Cart cartItem :cartListOrigin){
        CartItemVo item=new CartItemVo();
        item.setId(cartItem.getId());
        item.setUserId(cartItem.getUserId());
        item.setProductId(cartItem.getProductId());
        //获取产品信息
        Product product= productMapper.selectByPrimaryKey(cartItem.getId());
        if(product!=null){
          item.setPrice(product.getPrice());
          item.setMainImage(product.getMainImage());
          item.setProductName(product.getName());
          item.setStock(product.getStock());
          item.setProductSubTitle(product.getSubtitle());
          // 判断库存-------------------------------------------------------------
          int buyLimitCount=0; //可购买最大数
          if(product.getStock()>=item.getQuantity()){//商品数量大于等于购物车数量
            item.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
            buyLimitCount=item.getQuantity();
          }else{ //如果商品数不满足库存，更新购物车库存
            buyLimitCount=product.getStock();
            item.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
            //更新数据库中购物车有效库存
            Cart cart=new Cart();
            cart.setQuantity(buyLimitCount);
            cart.setId(cartItem.getId());
            cartMapper.updateByPrimaryKeySelective(cart);
          }

          //更新购买数量
          item.setQuantity(buyLimitCount);
          //计算单个商品总价
          item.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),item.getQuantity()));
          item.setChecked(cartItem.getChecked());
        }
        if(cartItem.getChecked()==Const.Cart.CHECK_ON){//如果商品在购物车中被勾选了，价格总价到整个购物车总价中
          totalPrice=BigDecimalUtil.add(totalPrice.doubleValue(),item.getTotalPrice().doubleValue());
        }
        cartItemVoList.add(item);
      }
    }
    cartItemListVo.setCartItemVoList(cartItemVoList);
    cartItemListVo.setTotalPrice(totalPrice);
    cartItemListVo.setAllChecked(this.getAllCheckedStatus(userId));
    cartItemListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
    return cartItemListVo;
  }

  /**
   * 根据用户查询购物车是否为全选状态;
   * @param userId
   * @return
   */
  private boolean getAllCheckedStatus(Integer userId){
    if(userId==null){
      return false;
    }
    //取反
    return cartMapper.selectIsAllCheckedByUserId(userId)==0?true:false;
  }

  public ServerResponse<CartItemListVo> update(Integer userId,Integer productId,Integer count){
    if(productId==null || count==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }

    Cart cart=cartMapper.selectCartByUserIdAndProductId(userId,productId);
    if(cart!=null){
      cart.setQuantity(count);
    }
    cartMapper.updateByPrimaryKeySelective(cart);
    CartItemListVo cartItemListVOLimit=this.getCartItemListVOLimit(userId);
    return ServerResponse.createBySuccess(cartItemListVOLimit);
  }

  /**
   * 删除产品
   * @param userId
   * @param productIds
   * @return
   */
  public ServerResponse<CartItemListVo> delete(Integer userId,String productIds){
    if(StringUtils.isBlank(productIds)){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }

    List<String> productIdList= Splitter.on(",").splitToList(productIds);

    cartMapper.deleteProductByUserId(userId,productIdList);

    CartItemListVo cartItemListVOLimit=this.getCartItemListVOLimit(userId);
    return ServerResponse.createBySuccess(cartItemListVOLimit);
  }


  public ServerResponse<CartItemListVo> getList(Integer userId){
    CartItemListVo cartItemListVOLimit=this.getCartItemListVOLimit(userId);
    return ServerResponse.createBySuccess(cartItemListVOLimit);
  }

  //全选或非全选
  public ServerResponse<CartItemListVo> checkOrUnCheck(Integer userId, Integer checked,Integer productId){
    cartMapper.setCheckedOrUnCheckedProduct(userId,checked,productId);
    return this.getList(userId);
  };

  public ServerResponse<Integer> getCartNum(Integer userId){
    int _num=cartMapper.getCartNumByUserId(userId);
    return ServerResponse.createBySuccess(_num);
  }
}
