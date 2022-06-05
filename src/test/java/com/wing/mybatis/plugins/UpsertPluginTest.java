package com.wing.mybatis.plugins;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.helper.SqlHelper;
import com.wing.mybatis.sample.common.Status;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class UpsertPluginTest {

    @Test
    public void testUpsert() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final Date date = new Date();
                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("name", "Z");
                user.set("age", 20);
                user.set("location", "浙江省杭州市");
                user.set("status", Status.DELETE);
                user.set("createTime", date);
                user.set("updateTime", date);
                user.set("feature", "testUpsert".getBytes(StandardCharsets.UTF_8));
                //id为null，默认为插入
                final Object insertResult = userMapper.invoke("upsert", user.getTarget());
                Assert.assertEquals(insertResult, 1);

                user.set("id", 100L);
                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "upsert", user.getTarget());
                Assert.assertEquals(sql,
                    "insert into user "
                        + "( id, `name`, age, address, `status`, create_time, update_time ) "
                        + "values "
                        + "( 100, 'Z', 20, '浙江省杭州市', -1, '" + new Timestamp(date.getTime()) + "', '" + new Timestamp(date.getTime()) + "' ) "
                        + "on duplicate key "
                        + "update id = 100, `name` = 'Z', age = 20, address = '浙江省杭州市', `status` = -1, create_time = '" + new Timestamp(date.getTime()) + "', update_time = '" + new Timestamp(date.getTime()) + "'");
                //id值在表中不存在，则插入
                final Object insertResult2 = userMapper.invoke("upsert", user.getTarget());
                Assert.assertEquals(insertResult2, 1);

                user.set("location", "浙江省绍兴市");
                //id值在表中存在，更新记录
                final Object updateResult = userMapper.invoke("upsert", user.getTarget());
                Assert.assertEquals(updateResult, 2);
            }
        });
    }

    @Test
    public void testUpsertWithBLOBs() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final Date date = new Date();
                byte[] feature = "testUpsertWithBLOBs".getBytes(StandardCharsets.UTF_8);
                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("name", "Z");
                user.set("age", 20);
                user.set("location", "浙江省杭州市");
                user.set("status", Status.DELETE);
                user.set("createTime", date);
                user.set("updateTime", date);
                user.set("feature", feature);
                //id为null，默认为插入
                final Object insertResult = userMapper.invoke("upsertWithBLOBs", user.getTarget());
                Assert.assertEquals(insertResult, 1);

                user.set("id", 100L);
                final String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "upsertWithBLOBs", user.getTarget());
                Assert.assertEquals(sql,
                    "insert into user "
                        + "( id, `name`, age, address, `status`, create_time, update_time, feature ) "
                        + "values "
                        + "( 100, 'Z', 20, '浙江省杭州市', -1, '" + new Timestamp(date.getTime()) + "', '" + new Timestamp(date.getTime()) + "', ** STREAM DATA ** ) "
                        + "on duplicate key "
                        + "update id = 100, `name` = 'Z', age = 20, address = '浙江省杭州市', `status` = -1, create_time = '" + new Timestamp(date.getTime()) + "', update_time = '" + new Timestamp(date.getTime()) + "', feature = ** STREAM DATA **");
                //id值在表中不存在，则插入
                final Object insertResult2 = userMapper.invoke("upsertWithBLOBs", user.getTarget());
                Assert.assertEquals(insertResult2, 1);

                user.set("feature", "testUpsertWithBLOBs2".getBytes(StandardCharsets.UTF_8));
                //id值在表中存在，更新记录
                final Object updateResult = userMapper.invoke("upsertWithBLOBs", user.getTarget());
                Assert.assertEquals(updateResult, 2);
            }
        });
    }

    @Test
    public void testUpsertSelective() throws Exception {
        DBHelper.createDB("init.sql");
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/UpsertPlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final ObjectWrapper userMapper = new ObjectWrapper(sqlSession.getMapper(loader.loadClass(basePackage + ".UserMapper")));
                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("name", "Z");
                user.set("age", 20);
                user.set("location", "浙江省杭州市");

                //id为null，默认为插入
                final Object insertResult = userMapper.invoke("upsertSelective", user.getTarget());
                Assert.assertEquals(insertResult, 1);

                user.set("id", 100L);
                String sql = SqlHelper.getFormatMapperSql(userMapper.getTarget(), "upsertSelective", user.getTarget());
                Assert.assertEquals(sql, "insert into user ( id, `name`, age, address ) values ( 100, 'Z', 20, '浙江省杭州市' ) on duplicate key update id = 100, `name` = 'Z', age = 20, address = '浙江省杭州市'");

                //id值在表中不存在，则插入
                final Object insertResult2 = userMapper.invoke("upsertSelective", user.getTarget());
                Assert.assertEquals(insertResult2, 1);

                user.set("location", "浙江省绍兴市");
                //id值在表中存在，更新记录
                final Object updateResult = userMapper.invoke("upsertSelective", user.getTarget());
                Assert.assertEquals(updateResult, 2);
            }
        });
    }
}
