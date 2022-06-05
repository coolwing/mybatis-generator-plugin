package com.wing.mybatis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.wing.mybatis.helper.DBHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class SampleGenerator {

    private static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File childFile : files) {
                delete(childFile);
            }
        } else if (file.isFile()) {
            file.delete();
        }
    }

    @Test
    public void generate() throws Exception {
        this.generateFiles();
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    private void generateFiles() throws Exception {
        DBHelper.createDB("init.sql");

        File javaFile = new File("src/test/java/com/wing/mybatis/product");
        delete(javaFile);
        File xmlFile = new File("src/test/resources/mapper");
        delete(xmlFile);

        List<String> warnings = new ArrayList<>();
        File configFile = new File("src/test/resources/sample/mybatis-generator.xml");
        ConfigurationParser cp = new ConfigurationParser(warnings);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(cp.parseConfiguration(configFile), new DefaultShellCallback(true), warnings);
        myBatisGenerator.generate(null);
    }
}

