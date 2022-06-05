package com.wing.mybatis.sample;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import com.wing.mybatis.product.example.UserExample.Column;
import com.wing.mybatis.sample.common.Status;
import org.junit.Assert;
import org.junit.Test;

public class UpdateWithColumnPluginTest extends BasePluginTest {
    @Test
    public void test() throws Exception {
        resetDB();
        Date date = new Date();
        User user = new User(100L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        UserExample example = UserExample.newAndCreateCriteria().andIdGreaterThanOrEqualTo(9L).example();
        int num = userMapper.updateByExampleWithColumn(user, example, Column.NAME, Column.LOCATION);

        Assert.assertEquals(num, 2);

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(10L).example());
        User updatedUser = users.get(0);
        Assert.assertEquals(updatedUser.getName(), "X");
        Assert.assertEquals(updatedUser.getAge().intValue(), 20);
        Assert.assertEquals(updatedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(updatedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(updatedUser.getFeature());
    }

    @Test
    public void testWithId() throws Exception {
        resetDB();
        Date date = new Date();
        User user = new User(100L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        UserExample example = UserExample.newAndCreateCriteria().andIdEqualTo(10L).example();
        int num = userMapper.updateByExampleWithColumn(user, example, Column.ID, Column.NAME, Column.LOCATION);

        Assert.assertEquals(num, 1);

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(100L).example());
        User updatedUser = users.get(0);
        Assert.assertEquals(updatedUser.getName(), "X");
        Assert.assertEquals(updatedUser.getAge().intValue(), 20);
        Assert.assertEquals(updatedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(updatedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(updatedUser.getFeature());
    }

    @Test
    public void testUpdateByPrimaryKey() throws Exception {
        resetDB();
        Date date = new Date();
        User user = new User(10L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        int num = userMapper.updateByPrimaryKeyWithColumn(user, Column.NAME, Column.LOCATION);

        Assert.assertEquals(num, 1);

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(10L).example());
        User updatedUser = users.get(0);
        Assert.assertEquals(updatedUser.getName(), "X");
        Assert.assertEquals(updatedUser.getAge().intValue(), 20);
        Assert.assertEquals(updatedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(updatedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(updatedUser.getFeature());
    }

    @Test
    public void testWithEmpty() throws Exception {
        try {
            resetDB();
            Date date = new Date();
            User user = new User(100L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));
            UserExample example = UserExample.newAndCreateCriteria().andIdEqualTo(10L).example();
            int num = userMapper.updateByExampleWithColumn(user, example);
        } catch (Exception e) {
            Assert.assertEquals(MySQLSyntaxErrorException.class, e.getCause().getClass());
        }

    }
}
