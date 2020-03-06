package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {

  public static final String Token_PREFIX="token_";
  private static Logger logger = LoggerFactory.getLogger(TokenCache.class);


  //LRU算法
  private static LoadingCache<String,String> localCache= CacheBuilder.newBuilder()
      .initialCapacity(1000)
      .maximumSize(10000)
      .expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
        //默认家在数据实现，当key没有命中时，执行匿名实现
        @Override
        public String load(String s) throws Exception {
          return null;
        }
      });


  public static void setKey(String key, String value){
    localCache.put(key,value);
  }

  public static String getKey(String key){
    String value=null;
    try{
      value=localCache.get(key);
      if("null".equals(value)){
        return null;
      }
      return value;
    }catch (Exception e){
      logger.error("localCache get error",e);
    }

    return null;
  }

}
