package com.wing.mybatis.plugins;

import java.lang.reflect.Array;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import com.wing.mybatis.sample.common.Status;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class UpdateWithColumnPluginTest {

    private static void valid(ObjectWrapper userMapper) throws Exception {
        final Object result = userMapper.invoke("selectByPrimaryKey", 1L);
        Assert.assertNotNull(result);
        final ObjectWrapper user = new ObjectWrapper(result);
        Assert.assertEquals(user.get("id"), 1L);
        Assert.assertEquals(user.get("name"), "A");
        Assert.assertEquals(user.get("age"), 20);
        Assert.assertEquals(user.get("location"), "浙江省杭州市");
        Assert.assertEquals(user.get("status"), Status.AVAILABLE);
        Assert.assertNull(user.get("feature"));
    }

    @Test
    public void testUpdateByExampleWithColumn() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/UpdateWithColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
                final ObjectWrapper criteria = new ObjectWrapper(userExample.invoke("createCriteria"));
                criteria.invoke("andNameEqualTo", "A");

                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("age", 20);
                user.set("location", "浙江省杭州市");
                user.set("status", Status.DELETE);

                final ObjectWrapper Column_AGE = new ObjectWrapper(loader, basePackage + ".UserExample$Column#AGE");
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                final Object columns = Array.newInstance(Column_AGE.getTargetClass(), 2);
                Array.set(columns, 0, Column_AGE.getTarget());
                Array.set(columns, 1, Column_LOCATION.getTarget());

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "updateByExampleWithColumn", user.getTarget(), userExample.getTarget(), columns);
                Assert.assertEquals(sql, "update user set age = 20 , address = '浙江省杭州市' WHERE ( `name` = 'A' )");
                final Object result = userMapper.invoke("updateByExampleWithColumn", user.getTarget(), userExample.getTarget(), columns);
                Assert.assertEquals(result, 1);
                valid(userMapper);
            }
        });
    }

    @Test
    public void testUpdateByPrimaryKeyWithColumn() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/UpdateWithColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("id", 1L);
                user.set("age", 20);
                user.set("location", "浙江省杭州市");
                user.set("status", Status.DELETE);

                final ObjectWrapper Column_AGE = new ObjectWrapper(loader, basePackage + ".UserExample$Column#AGE");
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                final Object columns = Array.newInstance(Column_AGE.getTargetClass(), 2);
                Array.set(columns, 0, Column_AGE.getTarget());
                Array.set(columns, 1, Column_LOCATION.getTarget());

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "updateByPrimaryKeyWithColumn", user.getTarget(), columns);
                Assert.assertEquals(sql, "update user set age = 20 , address = '浙江省杭州市' where id = 1");
                final Object result = userMapper.invoke("updateByPrimaryKeyWithColumn", user.getTarget(), columns);
                Assert.assertEquals(result, 1);
                valid(userMapper);
            }
        });
    }
}