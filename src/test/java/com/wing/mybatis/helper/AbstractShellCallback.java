package com.wing.mybatis.helper;

import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.mybatis.generator.internal.DefaultShellCallback;

public abstract class AbstractShellCallback extends DefaultShellCallback {
    private MyBatisGeneratorTool tool;

    public AbstractShellCallback() {
        super(true);
    }

    public void setTool(MyBatisGeneratorTool tool) {
        this.tool = tool;
    }

    /**
     * 当所有生成当文件保存之后会被调用
     *
     * @param project
     */
    @Override
    public void refreshProject(String project) {
        tool.compile();
        try (SqlSession sqlSession = tool.getSqlSession()) {
            this.doTest(sqlSession, tool.getTargetClassLoader(), tool.getTargetPackage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * 文件生成后，执行测试方法的入口
     *
     * @param sqlSession
     * @param loader
     * @param basePackage
     * @throws Exception
     */
    public abstract void doTest(SqlSession sqlSession, ClassLoader loader, String basePackage) throws Exception;
}
