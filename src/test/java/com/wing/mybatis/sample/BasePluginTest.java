package com.wing.mybatis.sample;

import java.io.InputStream;

import com.wing.mybatis.helper.DBHelper;
import com.wing.mybatis.product.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;

public abstract class BasePluginTest {
    protected static UserMapper userMapper;
    private static SqlSession session;

    @BeforeClass
    public static void init() throws Exception {
        String resource = "sample/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        session = sqlSessionFactory.openSession(true);
        userMapper = session.getMapper(UserMapper.class);
    }

    protected static void resetDB() throws Exception {
        DBHelper.execute(session, "truncate table user;");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (1, 'A', 11, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:18:36');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (2, 'B', 12, '江苏省徐州市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:50');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (3, 'C', 13, '江苏省盐城市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:55');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (4, 'D', 14, '江苏省盐城市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:59');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (5, 'E', 15, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (6, 'F', 16, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (7, 'G', 17, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (8, 'H', 18, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (9, 'I', 19, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
        DBHelper.execute(session, "INSERT INTO `user` VALUES (10, 'J', 20, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');");
    }
}
