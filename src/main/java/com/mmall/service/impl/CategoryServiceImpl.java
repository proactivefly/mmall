package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;


@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

  @Autowired
  private CategoryMapper categoryMapper;

  //日志
//  private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

  private Logger logger=LoggerFactory.getLogger(CategoryServiceImpl.class);


  public ServerResponse addCategory(String categoryName,Integer parentId){
    if(parentId==null || StringUtils.isBlank(categoryName)){
      return ServerResponse.createByErrorMsg("参加品类错误");
    }
    Category category= new Category();
    category.setName(categoryName);
    category.setStatus(true);
    category.setParentId(parentId);
    int count =categoryMapper.insert(category);
    if(count>0){
      return ServerResponse.createBySuccessMsg("插入成功");
    }
    return ServerResponse.createByErrorMsg("插入失败");
  }


  public ServerResponse setCategory(String categoryName,Integer categoryId){
    if(categoryId==null || StringUtils.isBlank(categoryName)){
      return ServerResponse.createByErrorMsg("参加品类错误");
    }
    Category category= new Category();
    category.setId(categoryId);
    category.setName(categoryName);
    int count = categoryMapper.updateByPrimaryKeySelective(category);
    if(count>0){
      return ServerResponse.createBySuccessMsg("更新成功");
    }
    return ServerResponse.createByErrorMsg("更新失败");
  }

  public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
    List<Category> list=categoryMapper.selectCategoryChildrenByParentId(categoryId);

    if(CollectionUtils.isEmpty(list)){
      //打印一行日志
      logger.info("未找到当前分类的子分类");
    }
    return ServerResponse.createBySuccess(list);
  }

  public ServerResponse<List<Integer>> selectCategoryAndChildrenCategory(Integer categoryId){

    Set<Category> categorySet= Sets.newHashSet();
    //调用递归算法
    findChildrenCategory(categorySet,categoryId);
    // Lists guava里的方法，提供数据结构和方法的插件
    List<Integer> categoryIdList= Lists.newArrayList();

    if(categoryId!=null){
      for(Category categoryItem : categorySet){
        categoryIdList.add(categoryItem.getId());
      }
    }
   return ServerResponse.createBySuccess(categoryIdList);
  }

  //递归算法
  // set 结构直接可以排重 set guava里的数据结构
  private Set<Category> findChildrenCategory(Set<Category> categorySet ,Integer categoryId){
    Category category=categoryMapper.selectByPrimaryKey(categoryId);
    if(category!=null){
      categorySet.add(category);
    }
    //查找子节点,退出条件
    List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);

    //foreach循环
    for(Category categoryItem : categoryList ){
      findChildrenCategory(categorySet,categoryItem.getId());
    }
    return categorySet;
  }
}
