package com.wing.mybatis.plugins;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExampleEnhancedPluginTest {

    @BeforeClass
    public static void init() throws SQLException, IOException {
        DBHelper.createDB("init.sql");
    }

    /**
     * 测试example工厂方法
     */
    @Test
    public void testExample() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
                final ObjectWrapper userExampleCriteria = new ObjectWrapper(userExample.invoke("createCriteria"));

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExampleCriteria.invoke("example"));
                Assert.assertEquals(sql, "select id, `name`, age, address, `status`, create_time, update_time from user");
            }
        });
    }

    /**
     * 测试orderBy方法
     */
    @Test
    public void testOrderBy() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");

                final ObjectWrapper SortType_ASC = new ObjectWrapper(loader, basePackage + ".UserExample$SortType#ASC");
                Assert.assertEquals(SortType_ASC.invoke("getValue"), "asc");
                final ObjectWrapper SortType_DESC = new ObjectWrapper(loader, basePackage + ".UserExample$SortType#DESC");
                Assert.assertEquals(SortType_DESC.invoke("getValue"), "desc");

                userExample.invoke("orderBy", "id", SortType_DESC.getTarget());
                final ObjectWrapper Column_NAME = new ObjectWrapper(loader, basePackage + ".UserExample$Column#NAME");
                userExample.invoke("orderBy", Column_NAME.getTarget(), SortType_ASC.getTarget());

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
                Assert.assertEquals(sql, "select id, `name`, age, address, `status`, create_time, update_time from user order by id desc, `name` asc");
            }
        });
    }

    /**
     * 测试distinct方法
     */
    @Test
    public void testDistinct() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");

                final Object example = userExample.invoke("distinct", true);
                Assert.assertEquals(example.getClass().getTypeName(), basePackage + ".UserExample");

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
                Assert.assertEquals(sql, "select distinct id, `name`, age, address, `status`, create_time, update_time from user");
            }
        });
    }

    /**
     * 测试静态方法 newAndCreateCriteria
     */
    @Test
    public void testStaticCreateCriteria() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper userExampleCriteria = new ObjectWrapper(loader.loadClass(basePackage + ".UserExample").getMethod("newAndCreateCriteria").invoke(null));

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExampleCriteria.invoke("example"));
                Assert.assertEquals(sql, "select id, `name`, age, address, `status`, create_time, update_time from user");
            }
        });
    }

    /**
     * 测试when方法
     */
    @Test
    public void testWhen() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleEnhancedPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));

                final String whenTrueSql = testCriteriaWhenMethod(loader, basePackage, userMapper, true);
                Assert.assertEquals(whenTrueSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 0 )");

                final String whenFalseSql = testCriteriaWhenMethod(loader, basePackage, userMapper, false);
                Assert.assertEquals(whenFalseSql, "select id, `name`, age, address, `status`, create_time, update_time from user");

                final String whenOtherwiseTrueSql = testCriteriaWhenOtherwiseMethod(loader, basePackage, userMapper, true);
                Assert.assertEquals(whenOtherwiseTrueSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 0 )");

                final String whenOtherwiseFalseSql = testCriteriaWhenOtherwiseMethod(loader, basePackage, userMapper, false);
                Assert.assertEquals(whenOtherwiseFalseSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 1 )");

                final String exampleWhenTrueSql = testExampleWhenMethod(loader, basePackage, userMapper, true);
                Assert.assertEquals(exampleWhenTrueSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 0 )");

                final String exampleWhenFalseSql = testExampleWhenMethod(loader, basePackage, userMapper, false);
                Assert.assertEquals(exampleWhenFalseSql, "select id, `name`, age, address, `status`, create_time, update_time from user");

                final String exampleWhenOtherwiseTrueSql = testExampleWhenOtherwiseMethod(loader, basePackage, userMapper, true);
                Assert.assertEquals(exampleWhenOtherwiseTrueSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 0 )");

                final String exampleWhenOtherwiseFalseSql = testExampleWhenOtherwiseMethod(loader, basePackage, userMapper, false);
                Assert.assertEquals(exampleWhenOtherwiseFalseSql, "select id, `name`, age, address, `status`, create_time, update_time from user WHERE ( id = 1 )");

            }
        });
    }

    private String testCriteriaWhenMethod(ClassLoader loader, String basePackage, ObjectWrapper userMapper, boolean condition) throws Exception {
        final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
        final ObjectWrapper userExampleCriteria = new ObjectWrapper(userExample.invoke("createCriteria"));
        // 代理实现接口
        final Object criteriaThen = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$CriteriaWhen")}, new TestCriteriaWhenInvocationHandler(0));
        // 找到只有两个参数的when
        final List<Method> methods = userExampleCriteria.getMethods("when");
        final Optional<Method> methodOptional = methods.stream().filter(m -> m.getParameters().length == 2).findFirst();
        Assert.assertTrue(methodOptional.isPresent());
        methodOptional.get().invoke(userExampleCriteria.getTarget(), condition, criteriaThen);
        return SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
    }

    private String testCriteriaWhenOtherwiseMethod(ClassLoader loader, String basePackage, ObjectWrapper userMapper, boolean condition) throws Exception {
        final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
        final ObjectWrapper userExampleCriteria = new ObjectWrapper(userExample.invoke("createCriteria"));
        final Object criteriaThen = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$CriteriaWhen")}, new TestCriteriaWhenInvocationHandler(0));
        final Object criteriaOtherwise = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$CriteriaWhen")}, new TestCriteriaWhenInvocationHandler(1));
        final List<Method> methods = userExampleCriteria.getMethods("when");
        final Optional<Method> methodOptional = methods.stream().filter(m -> m.getParameters().length == 3).findFirst();
        Assert.assertTrue(methodOptional.isPresent());
        methodOptional.get().invoke(userExampleCriteria.getTarget(), condition, criteriaThen, criteriaOtherwise);
        return SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
    }

    private String testExampleWhenMethod(ClassLoader loader, String basePackage, ObjectWrapper userMapper, boolean condition) throws Exception {
        final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
        final Object exampleThen = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$ExampleWhen")}, new TestExampleWhenInvocationHandler(0));
        final List<Method> methods = userExample.getMethods("when");
        final Optional<Method> methodOptional = methods.stream().filter(m -> m.getParameters().length == 2).findFirst();
        Assert.assertTrue(methodOptional.isPresent());
        methodOptional.get().invoke(userExample.getTarget(), condition, exampleThen);
        return SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
    }

    private String testExampleWhenOtherwiseMethod(ClassLoader loader, String basePackage, ObjectWrapper userMapper, boolean condition) throws Exception {
        final ObjectWrapper userExample = new ObjectWrapper(loader, basePackage + ".UserExample");
        final Object exampleThen = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$ExampleWhen")}, new TestExampleWhenInvocationHandler(0));
        final Object exampleOtherwise = Proxy.newProxyInstance(loader, new Class[] {loader.loadClass(basePackage + ".UserExample$ExampleWhen")}, new TestExampleWhenInvocationHandler(1));
        final List<Method> methods = userExample.getMethods("when");
        final Optional<Method> methodOptional = methods.stream().filter(m -> m.getParameters().length == 3).findFirst();
        Assert.assertTrue(methodOptional.isPresent());
        methodOptional.get().invoke(userExample.getTarget(), condition, exampleThen, exampleOtherwise);
        return SqlHelper.getFormatMapperSql(userMapper.getTarget(), "selectByExample", userExample.getTarget());
    }

    /**
     * CriteriaWhen代理实现
     */
    private static class TestCriteriaWhenInvocationHandler implements InvocationHandler {
        private final Integer arg;

        public TestCriteriaWhenInvocationHandler(Integer arg) {
            this.arg = arg;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("criteria".equals(method.getName())) {
                final ObjectWrapper userExampleCriteria = new ObjectWrapper(args[0]);
                userExampleCriteria.invoke("andIdEqualTo", Long.valueOf(arg));
                return userExampleCriteria.getTarget();
            }
            return null;
        }
    }

    /**
     * ExampleWhen代理实现
     */
    private static class TestExampleWhenInvocationHandler implements InvocationHandler {
        private final Integer arg;

        public TestExampleWhenInvocationHandler(Integer arg) {
            this.arg = arg;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("example".equals(method.getName())) {
                final ObjectWrapper userExample = new ObjectWrapper(args[0]);
                final ObjectWrapper criteria = new ObjectWrapper(userExample.invoke("or"));
                criteria.invoke("andIdEqualTo", Long.valueOf(arg));
                return userExample.getTarget();
            }
            return null;
        }
    }
}
