package com.wing.mybatis.plugins;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.wing.mybatis.helper.AbstractShellCallback;
import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import com.wing.mybatis.helper.ObjectWrapper;
import com.wing.mybatis.sample.common.Status;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class LombokPluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testGenerateDefault() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LombokPlugin/mybatis-generator-default.xml");
        final MyBatisGenerator myBatisGenerator = tool.generate();
        final List<String> all = Arrays.asList("@Data", "@NoArgsConstructor", "@AllArgsConstructor", "@Builder");
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            final CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof TopLevelClass) {
                final TopLevelClass topLevelClass = (TopLevelClass)compilationUnit;
                final String name = topLevelClass.getType().getShortName();
                if ("User".equals(name)) {
                    final List<String> annotations = topLevelClass.getAnnotations();
                    Assert.assertEquals(annotations.size(), all.size());
                }
            }
        }
    }

    @Test
    public void testGenerateWithOption() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LombokPlugin/mybatis-generator.xml");
        final MyBatisGenerator myBatisGenerator = tool.generate();
        for (GeneratedJavaFile file : myBatisGenerator.getGeneratedJavaFiles()) {
            final CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof TopLevelClass) {
                final TopLevelClass topLevelClass = (TopLevelClass)compilationUnit;
                final String name = topLevelClass.getType().getShortName();
                if ("User".equals(name)) {
                    final List<String> annotations = topLevelClass.getAnnotations();
                    Assert.assertEquals(annotations.size(), 1);
                    Assert.assertEquals(annotations.get(0), "@Data");
                }
            }
        }
    }

    /**
     * 测试 @Data 注解
     */
    @Test
    public void testDataAnnotation() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LombokPlugin/mybatis-generator-default.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                // get & set
                final ObjectWrapper user = new ObjectWrapper(loader, basePackage + ".User");
                user.invoke("setName", "data");
                Assert.assertEquals(user.invoke("getName"), "data");

                // equals & hash & toString
                final ObjectWrapper user1 = new ObjectWrapper(loader, basePackage + ".User");
                user1.invoke("setName", "equals_hashCode");
                final ObjectWrapper user2 = new ObjectWrapper(loader, basePackage + ".User");
                user2.invoke("setName", "equals_hashCode");
                Assert.assertEquals(user1.invoke("equals", user2.getTarget()), true);
                Assert.assertEquals(user1.invoke("hashCode"), user2.invoke("hashCode"));
                Assert.assertEquals(user1.invoke("toString"), "User(id=null, name=equals_hashCode, age=null, location=null, status=null, createTime=null, updateTime=null, feature=null)");
            }
        });
    }

    /**
     * 测试 @Builder 注解
     */
    @Test
    public void testBuilderAnnotation() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LombokPlugin/mybatis-generator-default.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                ObjectWrapper userBuilder = new ObjectWrapper(loader.loadClass(basePackage + ".User").getMethod("builder").invoke(null));
                userBuilder = new ObjectWrapper(userBuilder.invoke("id", 1L));
                userBuilder.invoke("name", "builder");
                final ObjectWrapper user = new ObjectWrapper(userBuilder.invoke("build"));
                Assert.assertEquals(user.invoke("toString"), "User(id=1, name=builder, age=null, location=null, status=null, createTime=null, updateTime=null, feature=null)");
            }
        });
    }

    /**
     * 测试 @NoArgsConstructor @AllArgsConstructor 注解
     */
    @Test
    public void testConstructorAnnotation() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/LombokPlugin/mybatis-generator-default.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception {
                final Class<?> clazz = loader.loadClass(basePackage + ".User");
                try {
                    // 无参
                    final ObjectWrapper user1 = new ObjectWrapper(clazz.newInstance());
                    Assert.assertEquals(user1.invoke("toString"), "User(id=null, name=null, age=null, location=null, status=null, createTime=null, updateTime=null, feature=null)");
                    // 有参
                    final Constructor<?> constructor = clazz.getConstructor(Long.class, String.class, Integer.class, String.class, Status.class, Date.class, Date.class, byte[].class);
                    final ObjectWrapper user2 = new ObjectWrapper(constructor.newInstance(102L, "constructor", null, null, null, null, null, null));
                    Assert.assertEquals(user2.invoke("toString"), "User(id=102, name=constructor, age=null, location=null, status=null, createTime=null, updateTime=null, feature=null)");
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        });
    }
}
