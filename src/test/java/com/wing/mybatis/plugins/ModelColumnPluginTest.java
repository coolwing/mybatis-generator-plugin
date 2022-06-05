package com.wing.mybatis.plugins;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelColumnPluginTest {

    @BeforeClass
    public static void init() throws SQLException, IOException {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testColumnEnum() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ModelColumnPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                // 1. 普通model
                final ObjectWrapper Column_ID = new ObjectWrapper(loader, basePackage + ".UserExample$Column#ID");
                Assert.assertEquals(Column_ID.invoke("getColumn"), "id");
                Assert.assertEquals(Column_ID.invoke("getJavaProperty"), "id");
                Assert.assertEquals(Column_ID.invoke("getJdbcType"), "BIGINT");
                Assert.assertEquals(Column_ID.invoke("getDelimitedColumnName"), "id");

                // 2. columnOverride
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                Assert.assertEquals(Column_LOCATION.invoke("getColumn"), "address");
                Assert.assertEquals(Column_LOCATION.invoke("getJavaProperty"), "location");
                Assert.assertEquals(Column_LOCATION.invoke("getJdbcType"), "VARCHAR");
                Assert.assertEquals(Column_LOCATION.invoke("getDelimitedColumnName"), "address");

                // 3. blob字段
                final ObjectWrapper Column_FEATURE = new ObjectWrapper(loader, basePackage + ".UserExample$Column#FEATURE");
                Assert.assertEquals(Column_FEATURE.invoke("getColumn"), "feature");
                Assert.assertEquals(Column_FEATURE.invoke("getJavaProperty"), "feature");
                Assert.assertEquals(Column_FEATURE.invoke("getJdbcType"), "LONGVARBINARY");
                Assert.assertEquals(Column_FEATURE.invoke("getDelimitedColumnName"), "feature");

                //4.ofColumn 方法
                final Object ofNameResult = (MethodUtils.invokeStaticMethod(Class.forName(basePackage + ".UserExample$Column"), "ofColumn", "status"));
                final ObjectWrapper Column_STATUS = new ObjectWrapper(ofNameResult);
                Assert.assertEquals(Column_STATUS.invoke("getColumn"), "status");
                Assert.assertEquals(Column_STATUS.invoke("getDelimitedColumnName"), "`status`");

                //5.excludes 方法
                //5.1 不排除
                final Object columns = Array.newInstance(Column_ID.getTargetClass(), 0);
                final Object[] result = (Object[])(MethodUtils.invokeStaticMethod(Class.forName(basePackage + ".UserExample$Column"), "excludes", columns));
                Assert.assertEquals(result.length, 8);

                //5.2 排除2个
                final Object columns2 = Array.newInstance(Column_ID.getTargetClass(), 2);
                Array.set(columns2, 0, Column_ID.getTarget());
                Array.set(columns2, 1, Column_LOCATION.getTarget());
                final Object[] result2 = (Object[])(MethodUtils.invokeStaticMethod(Class.forName(basePackage + ".UserExample$Column"), "excludes", columns2));
                Assert.assertEquals(result2.length, 6);
                for (Object obj : result2) {
                    final ObjectWrapper column = new ObjectWrapper(obj);
                    if (column.invoke("getColumn").equals("id") || column.invoke("getColumn").equals("address")) {
                        Assert.fail();
                    }
                }
            }
        });
    }
}
