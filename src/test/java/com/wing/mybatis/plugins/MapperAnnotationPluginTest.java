package com.wing.mybatis.plugins;

import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.helper.MyBatisGeneratorTool;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

public class MapperAnnotationPluginTest {

    @BeforeClass
    public static void init() throws Exception {
        DBHelper.createDB("init.sql");
    }

    @Test
    public void testMapperAnnotation() throws Exception {
        final MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("plugins/MapperAnnotationPlugin/mybatis-generator.xml");
        final MyBatisGenerator generator = tool.generate();

        for (GeneratedJavaFile file : generator.getGeneratedJavaFiles()) {
            final CompilationUnit compilationUnit = file.getCompilationUnit();
            if (compilationUnit instanceof Interface && compilationUnit.getType().getShortName().endsWith("Mapper")) {
                Interface interfaze = (Interface)compilationUnit;
                Assert.assertEquals(interfaze.getAnnotations().size(), 1);
                Assert.assertEquals(interfaze.getAnnotations().get(0), "@Mapper");
                Assert.assertTrue(interfaze.getImportedTypes().contains(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper")));
            }
        }
    }

}