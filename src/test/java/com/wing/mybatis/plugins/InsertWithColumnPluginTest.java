package com.wing.mybatis.plugins;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import com.wing.mybatis.sample.common.Status;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InsertWithColumnPluginTest {

    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("init.sql");
    }

    /**
     * 检查对象值是否正确
     *
     * @param userMapper
     * @param id
     * @throws Exception
     */
    private static void valid(ObjectWrapper userMapper, Long id) throws Exception {
        final Object result = userMapper.invoke("selectByPrimaryKey", id);
        Assert.assertNotNull(result);
        final ObjectWrapper user = new ObjectWrapper(result);
        Assert.assertEquals(user.get("id"), id);
        Assert.assertEquals(user.get("name"), "Z");
        Assert.assertEquals(user.get("age"), 20);
        Assert.assertEquals(user.get("location"), "浙江省杭州市");
        Assert.assertEquals(user.get("status"), Status.DISABLE);
        Assert.assertNull(user.get("feature"));
    }

    @Test
    public void testInsertWithColumn() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/InsertWithColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("name", "Z");
                user.set("age", 20);
                user.set("location", "浙江省杭州市");
                user.set("status", Status.DELETE);

                final ObjectWrapper Column_NAME = new ObjectWrapper(loader, basePackage + ".UserExample$Column#NAME");
                final ObjectWrapper Column_AGE = new ObjectWrapper(loader, basePackage + ".UserExample$Column#AGE");
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                final Object columns = Array.newInstance(Column_AGE.getTargetClass(), 3);
                Array.set(columns, 0, Column_NAME.getTarget());
                Array.set(columns, 1, Column_AGE.getTarget());
                Array.set(columns, 2, Column_LOCATION.getTarget());

                // sql
                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "insertWithColumn", user.getTarget(), columns);
                Assert.assertEquals(sql, "insert into user ( `name` , age , address ) values ( 'Z' , 20 , '浙江省杭州市' )");
                final Object result = userMapper.invoke("insertWithColumn", user.getTarget(), columns);
                Assert.assertEquals(result, 1);

                valid(userMapper, (Long)user.get("id"));
            }
        });
    }
}