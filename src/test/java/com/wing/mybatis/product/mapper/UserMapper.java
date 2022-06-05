package com.wing.mybatis.product.mapper;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertWithColumn(@Param("record") User record, @Param("columns") UserExample.Column ... columns);

    int insertSelective(User record);

    List<User> selectByExampleWithBLOBs(UserExample example);

    List<User> selectByExample(UserExample example);

    List<User> selectByExampleWithColumn(@Param("example") UserExample example, @Param("columns") UserExample.Column ... columns);

    User selectByPrimaryKey(Long id);

    User selectByPrimaryKeyWithColumn(@Param("id") Long id, @Param("columns") UserExample.Column ... columns);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExampleWithBLOBs(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByExampleWithColumn(@Param("record") User record, @Param("example") UserExample example, @Param("columns") UserExample.Column ... columns);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKeyWithBLOBs(User record);

    int updateByPrimaryKey(User record);

    int updateByPrimaryKeyWithColumn(@Param("record") User record, @Param("columns") UserExample.Column ... columns);

    int batchInsert(@Param("list") List<User> list);

    int batchInsertWithColumn(@Param("list") List<User> list, @Param("columns") UserExample.Column ... columns);

    int upsert(User record);

    int upsertSelective(User record);

    int upsertWithBLOBs(User record);
}