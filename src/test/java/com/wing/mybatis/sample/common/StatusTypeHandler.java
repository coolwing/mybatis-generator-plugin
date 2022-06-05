/*
 * Copyright 2018 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */

package com.wing.mybatis.sample.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * @author wing
 * @date 2022/5/2
 **/
@MappedTypes(Status.class)
public class StatusTypeHandler extends BaseTypeHandler<Status> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Status parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getIndex());
    }

    /**
     * 获取数据结果集时把数据库类型转换为对应的Java类型
     *
     * @param rs         当前的结果集
     * @param columnName 当前的字段名称
     * @return 转换后的Java对象
     * @throws SQLException
     */
    @Override
    public Status getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final int value = rs.getInt(columnName);
        //把数据库数值转化为对应的枚举
        return convert(value);
    }

    /**
     * 通过字段位置获取字段数据时把数据库类型转换为对应的Java类型
     *
     * @param rs          当前的结果集
     * @param columnIndex 当前字段的位置
     * @return 转换后的Java对象
     * @throws SQLException
     */
    @Override
    public Status getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final int value = rs.getInt(columnIndex);
        return convert(value);
    }

    /**
     * 调用存储过程后把数据库类型的数据转换为对应的Java类型
     *
     * @param cs          当前的CallableStatement执行后的CallableStatement
     * @param columnIndex 当前输出参数的位置
     * @return
     * @throws SQLException
     */
    @Override
    public Status getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        final int value = cs.getInt(columnIndex);
        return convert(value);
    }

    private Status convert(int value) {
        return Status.of(value);
    }

}
