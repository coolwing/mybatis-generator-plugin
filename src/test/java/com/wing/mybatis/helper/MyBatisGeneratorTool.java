package com.wing.mybatis.helper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MyBatisGeneratorTool {
    public final static String DAO_PACKAGE = "com.wing.dao";
    private Configuration config;
    private String targetProject;
    private String targetPackage;

    /**
     * 创建
     * @param resource
     * @return
     */
    public static MyBatisGeneratorTool create(String resource) throws IOException, XMLParserException {
        final MyBatisGeneratorTool tool = new MyBatisGeneratorTool();
        final ConfigurationParser config = new ConfigurationParser(new ArrayList<>());
        tool.config = config.parseConfiguration(Resources.getResourceAsStream(resource));
        // 修正配置目标
        tool.fixConfigToTarget();
        return tool;
    }

    /**
     * 执行MyBatisGenerator(不生成文件)
     *
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate() throws Exception {
        final MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        myBatisGenerator.generate(null, null, null, false);
        return myBatisGenerator;
    }

    /**
     * 执行MyBatisGenerator
     *
     * @param callback
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public MyBatisGenerator generate(AbstractShellCallback callback) throws Exception {
        callback.setTool(this);
        final MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, new ArrayList<>());
        myBatisGenerator.generate(null, null, null, true);
        return myBatisGenerator;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    /**
     * 编译项目并返回 SqlSession
     *
     * @return
     */
    public void compile() {
        // 动态编译java文件
        final String target = targetProject + targetPackage.replaceAll("\\.", "/");
        final List<File> javaFiles = getGeneratedFiles(new File(target), ".java");
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        //获取java文件管理类
        final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        //设置编译参数
        final ArrayList<String> ops = new ArrayList<>();
        ops.add("-Xlint:unchecked");
        //获取编译任务
        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, manager.getJavaFileObjectsFromFiles(javaFiles));
        //执行编译任务
        task.call();
    }

    /**
     * 获取目标目录的ClassLoader
     * @return
     */
    public ClassLoader getTargetClassLoader() throws MalformedURLException {
        return URLClassLoader.newInstance(new URL[] {new File(targetProject).toURI().toURL()});
    }

    /**
     * 获取SqlSession
     *
     * @return
     * @throws IOException
     */
    public SqlSession getSqlSession() {
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setCallSettersOnNulls(true);
        config.setMapUnderscoreToCamelCase(true);
        config.addMappers(targetPackage);
        PooledDataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
        dataSourceFactory.setProperties(DBHelper.properties);
        DataSource dataSource = dataSourceFactory.getDataSource();
        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();

        Environment environment = new Environment("test", transactionFactory, dataSource);
        config.setEnvironment(environment);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
        return sqlSessionFactory.openSession(true);
    }

    /**
     * 修正配置到指定target
     */
    private void fixConfigToTarget() {
        this.targetProject = this.getClass().getClassLoader().getResource("").getPath();
        this.targetPackage = DAO_PACKAGE + ".s" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        for (Context context : config.getContexts()) {
            context.getJavaModelGeneratorConfiguration().setTargetProject(targetProject);
            context.getJavaModelGeneratorConfiguration().setTargetPackage(targetPackage);
            context.getSqlMapGeneratorConfiguration().setTargetProject(targetProject);
            context.getSqlMapGeneratorConfiguration().setTargetPackage(targetPackage);
            context.getJavaClientGeneratorConfiguration().setTargetProject(targetProject);
            context.getJavaClientGeneratorConfiguration().setTargetPackage(targetPackage);
        }
    }


    /**
     * 获取指定后缀的文件
     * @param file
     * @return
     */
    private List<File> getGeneratedFiles(File file, String ext) {
        List<File> list = new ArrayList<>();
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    list.addAll(getGeneratedFiles(childFile, ext));
                } else if (childFile.getName().endsWith(ext)) {
                    list.add(childFile);
                }
            }
        }
        return list;
    }
}
