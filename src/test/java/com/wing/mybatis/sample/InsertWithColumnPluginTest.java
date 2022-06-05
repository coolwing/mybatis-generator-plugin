package com.wing.mybatis.sample;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import com.wing.mybatis.product.example.UserExample.Column;
import com.wing.mybatis.sample.common.Status;
import org.junit.Assert;
import org.junit.Test;

public class InsertWithColumnPluginTest extends BasePluginTest {

    /**
     * 对象中存在ID，插入时指定使用对象中的ID，则插入后数据为对象中ID
     *
     * @throws Exception
     */
    @Test
    public void testWithCertainId() throws Exception {
        BasePluginTest.resetDB();
        Date date = new Date();
        User user = new User(100L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        int num = userMapper.insertWithColumn(user, Column.ID, Column.NAME, Column.AGE, Column.LOCATION, Column.STATUS);
        Assert.assertEquals(num, 1);

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(100L).example());
        Assert.assertEquals(1, users.size());
        User insertedUser = users.get(0);
        Assert.assertEquals(insertedUser.getName(), "X");
        Assert.assertEquals(insertedUser.getAge().intValue(), 100);
        Assert.assertEquals(insertedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(insertedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(insertedUser.getFeature());
    }

    /**
     * 对象中【不】存在ID，插入时指定使用对象中的ID，则插入后数据为自增ID，且ID会直接填充到对象中
     *
     * @throws Exception
     */
    @Test
    public void testWithAutoGeneratedId() throws Exception {
        BasePluginTest.resetDB();
        Date date = new Date();
        User user = new User(null, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        int num = userMapper.insertWithColumn(user, Column.ID, Column.NAME, Column.AGE, Column.LOCATION, Column.STATUS);
        Assert.assertEquals(num, 1);

        Assert.assertEquals(11L, user.getId().longValue());

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(11L).example());
        Assert.assertEquals(1, users.size());
        User insertedUser = users.get(0);
        Assert.assertEquals(insertedUser.getName(), "X");
        Assert.assertEquals(insertedUser.getAge().intValue(), 100);
        Assert.assertEquals(insertedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(insertedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(insertedUser.getFeature());
    }

    /**
     * 对象中存在ID，插入时【不】指定使用对象中的ID，则插入后数据为自增ID，且ID会覆盖对象中的ID
     *
     * @throws Exception
     */
    @Test
    public void testWithOverwriteId() throws Exception {
        BasePluginTest.resetDB();
        Date date = new Date();
        User user = new User(100L, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        int num = userMapper.insertWithColumn(user, Column.NAME, Column.AGE, Column.LOCATION, Column.STATUS);
        Assert.assertEquals(num, 1);

        Assert.assertEquals(11L, user.getId().longValue());

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(11L).example());
        User insertedUser = users.get(0);
        Assert.assertEquals(insertedUser.getName(), "X");
        Assert.assertEquals(insertedUser.getAge().intValue(), 100);
        Assert.assertEquals(insertedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(insertedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(insertedUser.getFeature());
    }

    /**
     * 对象中【不】存在ID，插入时【不】指定使用对象中的ID，则插入后数据为自增ID，且ID会直接填充到对象中
     *
     * @throws Exception
     */
    @Test
    public void testWithNullId() throws Exception {
        BasePluginTest.resetDB();
        Date date = new Date();
        User user = new User(null, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        int num = userMapper.insertWithColumn(user, Column.NAME, Column.AGE, Column.LOCATION, Column.STATUS);
        Assert.assertEquals(num, 1);

        Assert.assertEquals(11L, user.getId().longValue());

        List<User> users = userMapper.selectByExample(UserExample.newAndCreateCriteria().andIdEqualTo(11L).example());
        User insertedUser = users.get(0);
        Assert.assertEquals(insertedUser.getName(), "X");
        Assert.assertEquals(insertedUser.getAge().intValue(), 100);
        Assert.assertEquals(insertedUser.getLocation(), "江苏省扬州市");
        Assert.assertEquals(insertedUser.getStatus(), Status.AVAILABLE);
        Assert.assertNull(insertedUser.getFeature());
    }

    /**
     * 插入时不指定列，则根据数据表中设置的默认值自动生成1条数据，插入对象中的非ID字段不变
     * 如果数据表中设置了非空值，则会抛异常
     *
     * @throws Exception
     */
    @Test
    public void testWithEmptyColumn() throws Exception {
        BasePluginTest.resetDB();
        Date date = new Date();
        try {
            User user = new User(null, "X", 100, "江苏省扬州市", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));
            int num = userMapper.insertWithColumn(user);
            Assert.assertEquals(1, num);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("Field 'name' doesn't have a default value", e.getCause().getLocalizedMessage());
        }
    }

}