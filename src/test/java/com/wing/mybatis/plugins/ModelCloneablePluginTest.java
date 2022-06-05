package com.wing.mybatis.plugins;

import java.util.Date;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelCloneablePluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testClone() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ModelCloneablePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final Date date = new Date();
                final Date date2 = new Date(date.getTime() + 60 * 1000);
                final Date date3 = new Date(date.getTime() + 3 * 60 * 1000);
                //构建原始对象
                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.set("id", 101L);
                user.set("name", "clone");
                user.set("createTime", date);

                //浅拷贝
                final ObjectWrapper userClone = new ObjectWrapper(user.invoke("clone"));
                Assert.assertEquals(userClone.get("id"), 101L);
                Assert.assertEquals(userClone.get("name"), "clone");
                Assert.assertEquals(userClone.get("createTime"), date);

                userClone.set("name", "other");
                Assert.assertEquals(user.get("name"), "clone");
                Assert.assertEquals(userClone.get("name"), "other");
                //修改浅拷贝的引用字段，原对象的字段也会发生改变
                Date createTimeForClone = (Date)userClone.invoke("getCreateTime");
                createTimeForClone.setTime(date3.getTime());
                Assert.assertEquals(user.get("createTime"), date3);

                //深拷贝
                final ObjectWrapper userDeepClone = new ObjectWrapper(user.invoke("deepClone"));
                Assert.assertEquals(userDeepClone.get("id"), 101L);
                Assert.assertEquals(userDeepClone.get("name"), "clone");
                Assert.assertEquals(userDeepClone.get("createTime"), date);

                userClone.set("name", "other");
                Assert.assertEquals(user.get("name"), "clone");
                Assert.assertEquals(userClone.get("name"), "other");
                //修改深拷贝的引用字段，原对象的字段不会发生改变
                final Date createTimeForDeepClone = (Date)userDeepClone.invoke("getCreateTime");
                createTimeForDeepClone.setTime(date2.getTime());
                Assert.assertEquals(userDeepClone.get("createTime"), date2);
                Assert.assertEquals(user.get("createTime"), date3);
            }
        });
    }

}