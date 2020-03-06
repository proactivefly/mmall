package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;


public interface UserMapper {
    //public abstract 省略了


    public abstract int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);
    //传入多个参数时，需要传入参数注解
    User selectLogin(@Param("username") String username, @Param("password") String password);


    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username, @Param("question")String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username,@Param("passwordNew") String passwordNew);
}