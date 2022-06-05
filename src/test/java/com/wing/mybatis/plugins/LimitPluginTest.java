package com.wing.mybatis.plugins;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LimitPluginTest {

    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testLimitMethod() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LimitPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");

                // 调用limit(int rows)方法
                userExample.invoke("limit", 2);
                final String limitSql1 = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
                Assert.assertEquals(limitSql1, "select id, `name`, age, address, `status`, create_time, update_time from user limit 2");
                // 执行
                final List<?> list1 = (List<?>)userMapper.invoke("selectByExample", userExample.getTarget());
                Assert.assertEquals(list1.size(), 2);
                Assert.assertEquals(new ObjectWrapper(list1.get(0)).get("id"), 1L);

                // 调用limit(int offset, int rows)方法
                userExample.invoke("limit", 1, 2);
                final String limitSql2 = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
                Assert.assertEquals(limitSql2, "select id, `name`, age, address, `status`, create_time, update_time from user limit 1, 2");
                // 执行
                final List<?> list2 = (List<?>)userMapper.invoke("selectByExample", userExample.getTarget());
                Assert.assertEquals(list2.size(), 2);
                Assert.assertEquals(new ObjectWrapper(list2.get(0)).get("id"), 2L);
            }
        });
    }

    @Test
    public void testPageMethod() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LimitPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");

                userExample.invoke("page", 2, 2);
                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
                Assert.assertEquals(sql, "select id, `name`, age, address, `status`, create_time, update_time from user limit 2, 2");

                final List<?> list3 = (List<?>)userMapper.invoke("selectByExample", userExample.getTarget());
                Assert.assertEquals(list3.size(), 2);
                Assert.assertEquals(new ObjectWrapper(list3.get(0)).get("id"), 3L);
            }
        });
    }
}
