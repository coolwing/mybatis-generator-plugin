package com.wing.mybatis.plugins;

import java.util.List;

import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;

public class ExampleTargetPluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testExampleTargetPath() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/ExampleTargetPlugin/mybatis-generator.xml");
        final MyBatisGenerator generator = tool.generate();

        final List<GeneratedJavaFile> list = generator.getGeneratedJavaFiles();
        for (GeneratedJavaFile file : list) {
            if (file.getFileName().equals("UserExample.java")) {
                Assert.assertEquals(file.getTargetPackage(), "com.wing.dao.example");
            }
        }
    }
}
