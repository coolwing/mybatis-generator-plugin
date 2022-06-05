package com.wing.mybatis.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

public class DBHelper {
    private static final String DB_CONFIG = "db.properties";
    public static Properties properties;
    private static Connection connection;

    static {
        try {
            properties = new Properties();
            try (InputStream inputStream = Resources.getResourceAsStream(DB_CONFIG)) {
                properties.load(inputStream);
            }
            // 数据库连接
            final String driver = properties.getProperty("driver");
            final String url = properties.getProperty("url");
            final String username = properties.getProperty("username");
            final String password = properties.getProperty("password");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建数据库，并插入初始数据
     *
     * @param resource
     * @throws SQLException
     * @throws IOException
     */
    public static void createDB(String resource) throws SQLException, IOException {
        try (
            final Statement statement = connection.createStatement();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Resources.getResourceAsStream(resource), StandardCharsets.UTF_8));
        ) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            statement.execute(sb.toString());
        }
    }

    /**
     * 执行sql
     *
     * @param sqlSession
     * @param sql
     * @return
     * @throws SQLException
     */
    public static ResultSet execute(SqlSession sqlSession, String sql) throws SQLException {
        return execute(sqlSession.getConnection(), sql);
    }

    /**
     * 执行sql
     *
     * @param connection
     * @param sql
     * @return
     * @throws SQLException
     */
    public static ResultSet execute(Connection connection, String sql) throws SQLException {
        final Statement statement = connection.createStatement();
        statement.execute(sql);
        return statement.getResultSet();
    }

    @Test
    public void testGetSqlSession() throws Exception {
        DBHelper.createDB("init.sql");
        final ResultSet resultSet = DBHelper.execute(connection, "SELECT COUNT(*) as total FROM user");
        resultSet.first();
        Assert.assertEquals(resultSet.getInt("total"), 10);
        connection.close();
    }

}
