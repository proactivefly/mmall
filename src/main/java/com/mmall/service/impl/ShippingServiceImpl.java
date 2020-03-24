package com.mmall.service.impl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("iShippingService") //注入controller

public class ShippingServiceImpl implements IShippingService {
  @Autowired
  private ShippingMapper shippingMapper;

  public ServerResponse add(Integer userId, Shipping shipping){
    shipping.setUserId(userId);
    int rowCount=shippingMapper.insert(shipping);
    if(rowCount>0){
      Map maps= Maps.newHashMap();
      maps.put("shippingId",shipping.getId());
      return ServerResponse.createBySuccess("新增地址成功",maps);
    }else{
      return ServerResponse.createByErrorMsg("新增地址失败");
    }
  }

  public ServerResponse delete(Integer userId,Integer shippingId){
    if(shippingId==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }
    int resultCount=shippingMapper.deleteAddByUserId(userId,shippingId);
    if(resultCount>0){
      return ServerResponse.createBySuccessMsg("删除地址成功");
    }else{
      return ServerResponse.createByErrorMsg("删除地址失败");
    }
  }


  public ServerResponse update(Integer userId,Shipping shipping){
    if(shipping==null){
      return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }

    shipping.setUserId(userId);//获取登录用户的userId,避免横向越权

    int resultCount=shippingMapper.updateShippintByUserId(shipping);
    if(resultCount>0){
      return ServerResponse.createBySuccessMsg("更新地址成功");
    }else{
      return ServerResponse.createByErrorMsg("更新地址失败");
    }
  }

  public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
    PageHelper.startPage(pageNum,pageSize);
    List<Shipping> resultList=shippingMapper.getListByUserId(userId);
    PageInfo pageInfo =new PageInfo(resultList);
    return ServerResponse.createBySuccess(pageInfo);
  }
}
