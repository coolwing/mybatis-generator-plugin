package com.wing.mybatis.plugins;

import java.lang.reflect.Array;
import java.util.Arrays;
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

public class SelectWithColumnPluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testSelectByExampleWithColumn() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/SelectWithColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
                final ObjectWrapper criteria = new ObjectWrapper(userExample.invoke("createCriteria"));
                criteria.invoke("andIdLessThan", 100L);
                userExample.set("orderByClause", "id asc");

                final ObjectWrapper Column_ID = new ObjectWrapper(loader, basePackage + ".UserExample$Column#ID");
                final Object columns1 = Array.newInstance(Column_ID.getTargetClass(), 1);
                Array.set(columns1, 0, Column_ID.getTarget());

                final String column1Sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExampleWithColumn", userExample.getTarget(), columns1);
                Assert.assertEquals(column1Sql, "select id from user WHERE ( id < 100 ) order by id asc");

                final ObjectWrapper Column_NAME = new ObjectWrapper(loader, basePackage + ".UserExample$Column#NAME");
                final Object columns2 = Array.newInstance(Column_ID.getTargetClass(), 2);
                Array.set(columns2, 0, Column_ID.getTarget());
                Array.set(columns2, 1, Column_NAME.getTarget());

                final String column2Sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExampleWithColumn", userExample.getTarget(), columns2);
                Assert.assertEquals(column2Sql, "select id , `name` from user WHERE ( id < 100 ) order by id asc");

                //执行sql
                final List<?> list = (List<?>)userMapper.invoke("selectByExampleWithColumn", userExample.getTarget(), columns2);
                Assert.assertEquals(list.size(), 10);
                for (int i = 0; i < list.size(); i++) {
                    Assert.assertNotNull(list.get(i));
                    ObjectWrapper user = new ObjectWrapper(list.get(i));
                    Assert.assertEquals(user.get("id"), (long)(i + 1));
                    Assert.assertEquals(user.get("name"), String.valueOf((char)('A' + i)));
                    Assert.assertNull(user.get("age"));
                    Assert.assertNull(user.get("location"));
                    Assert.assertNull(user.get("status"));
                    Assert.assertNull(user.get("feature"));
                    Assert.assertNull(user.get("createTime"));
                    Assert.assertNull(user.get("updateTime"));
                }

                //测试 distinct
                userExample.invoke("setDistinct", true);
                userExample.set("orderByClause", "address asc");
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                final Object columns3 = Array.newInstance(Column_LOCATION.getTargetClass(), 1);
                Array.set(columns3, 0, Column_LOCATION.getTarget());
                final String distinctSql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExampleWithColumn", userExample.getTarget(), columns3);
                Assert.assertEquals(distinctSql, "select distinct address from user WHERE ( id < 100 ) order by address asc");

                final List<?> distinctList = (List<?>)userMapper.invoke("selectByExampleWithColumn", userExample.getTarget(), columns3);
                Assert.assertEquals(distinctList.size(), 3);
                for (Object o : distinctList) {
                    Assert.assertNotNull(o);
                    ObjectWrapper user = new ObjectWrapper(o);
                    Assert.assertNull(user.get("id"));
                    Assert.assertNull(user.get("name"));
                    Assert.assertNull(user.get("age"));
                    Assert.assertNull(user.get("status"));
                    Assert.assertNull(user.get("feature"));
                    Assert.assertNull(user.get("createTime"));
                    Assert.assertNull(user.get("updateTime"));
                    Assert.assertTrue(Arrays.asList("江苏省南京市", "江苏省徐州市", "江苏省盐城市").contains(user.get("location")));
                }
            }
        });
    }

    @Test
    public void testSelectByPrimaryKeyWithColumn() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/SelectWithColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final ObjectWrapper Column_NAME = new ObjectWrapper(loader, basePackage + ".UserExample$Column#NAME");
                final Object columns1 = Array.newInstance(Column_NAME.getTargetClass(), 1);
                Array.set(columns1, 0, Column_NAME.getTarget());

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByPrimaryKeyWithColumn", 1, columns1);
                Assert.assertEquals(sql, "select `name` from user where id = 1");

                //执行sql
                final Object user = userMapper.invoke("selectByPrimaryKeyWithColumn", 1L, columns1);
                Assert.assertEquals(new ObjectWrapper(user).get("name"), "A");
            }
        });
    }
}
