package com.wing.mybatis.plugins;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import com.wing.mybatis.sample.common.Status;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class BatchInsertPluginTest {

    @Test
    public void testBatchInsert() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/BatchInsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                //构建参数
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final Date date = new Date();
                final List<Object> params = new ArrayList<>();
                params.add(
                    new ObjectWrapper(loader, basePackage + ".User")
                        .set("name", "Z")
                        .set("age", 20)
                        .set("location", "浙江省杭州市")
                        .set("status", Status.DELETE)
                        .set("createTime", date)
                        .set("updateTime", date)
                        .getTarget()
                );
                params.add(
                    new ObjectWrapper(loader, basePackage + ".User")
                        .set("name", "Y")
                        .set("age", 21)
                        .set("location", "浙江省绍兴市")
                        .set("status", Status.AVAILABLE)
                        .set("createTime", date)
                        .set("updateTime", date)
                        .getTarget()
                );

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "batchInsert", params);
                Assert.assertEquals(sql,
                    "insert into user ( `name`, age, address, `status`, create_time, update_time, feature ) "
                        + "values "
                        + "( 'Z', 20, '浙江省杭州市', -1, '" + new Timestamp(date.getTime()) + "', '" + new Timestamp(date.getTime()) + "', null ) , "
                        + "( 'Y', 21, '浙江省绍兴市', 1, '" + new Timestamp(date.getTime()) + "', '" + new Timestamp(date.getTime()) + "', null )");
                //执行sql
                final Object count = userMapper.invoke("batchInsert", params);
                Assert.assertEquals(count, 2);
            }
        });
    }

    @Test
    public void testBatchInsertWithColumn() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/BatchInsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                //构建参数
                final Date date = new Date();
                final List<Object> params = new ArrayList<>();
                params.add(
                    new ObjectWrapper(loader, basePackage + ".User")
                        .set("name", "Z")
                        .set("age", 20)
                        .set("location", "浙江省杭州市")
                        .set("status", Status.DELETE)
                        .set("createTime", date)
                        .set("updateTime", date)
                        .getTarget()
                );
                params.add(
                    new ObjectWrapper(loader, basePackage + ".User")
                        .set("name", "Y")
                        .set("age", 21)
                        .set("location", "浙江省绍兴市")
                        .set("status", Status.AVAILABLE)
                        .set("createTime", date)
                        .set("updateTime", date)
                        .getTarget()
                );

                final ObjectWrapper Column_NAME = new ObjectWrapper(loader, basePackage + ".UserExample$Column#NAME");
                final ObjectWrapper Column_AGE = new ObjectWrapper(loader, basePackage + ".UserExample$Column#AGE");
                final ObjectWrapper Column_LOCATION = new ObjectWrapper(loader, basePackage + ".UserExample$Column#LOCATION");
                final Object columns = Array.newInstance(Column_AGE.getTargetClass(), 3);
                Array.set(columns, 0, Column_NAME.getTarget());
                Array.set(columns, 1, Column_AGE.getTarget());
                Array.set(columns, 2, Column_LOCATION.getTarget());

                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "batchInsertWithColumn", params, columns);
                Assert.assertEquals(sql, "insert into user ( `name` , age , address ) values ( 'Z' , 20 , '浙江省杭州市' ) , ( 'Y' , 21 , '浙江省绍兴市' )");

                //执行sql
                final Object count = userMapper.invoke("batchInsertWithColumn", params, columns);
                Assert.assertEquals(count, 2);
            }
        });
    }

}
