package com.wing.mybatis.sample;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import com.wing.mybatis.product.example.UserExample.Column;
import com.wing.mybatis.sample.common.Status;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SelectWithColumnPluginTest extends BasePluginTest {

    @BeforeClass
    public static void reset() throws Exception {
        BasePluginTest.resetDB();
    }

    @Test
    public void testByPrimaryKey() {
        User user = userMapper.selectByPrimaryKeyWithColumn(2L, Column.NAME, Column.STATUS);
        Assert.assertNull(user.getId());
        Assert.assertEquals(user.getName(), "B");
        Assert.assertNull(user.getAge());
        Assert.assertNull(user.getLocation());
        Assert.assertEquals(user.getStatus(), Status.AVAILABLE);
        Assert.assertNull(user.getFeature());
        Assert.assertNull(user.getCreateTime());
        Assert.assertNull(user.getUpdateTime());
    }

    @Test
    public void testByExample() {
        UserExample example = UserExample.newAndCreateCriteria()
            .andIdGreaterThanOrEqualTo(2L)
            .example();
        List<User> users = userMapper.selectByExampleWithColumn(example, Column.NAME, Column.STATUS);
        Assert.assertEquals(users.size(), 9);
        User user = users.get(0);
        Assert.assertNull(user.getId());
        Assert.assertEquals(user.getName(), "B");
        Assert.assertNull(user.getAge());
        Assert.assertNull(user.getLocation());
        Assert.assertEquals(user.getStatus(), Status.AVAILABLE);
        Assert.assertNull(user.getFeature());
        Assert.assertNull(user.getCreateTime());
        Assert.assertNull(user.getUpdateTime());

    }

    @Test
    public void testColumnExcludes() {
        UserExample example = UserExample.newAndCreateCriteria()
            .andIdGreaterThanOrEqualTo(2L)
            .example();
        List<User> users = userMapper.selectByExampleWithColumn(example, Column.excludes(Column.UPDATE_TIME));
        Assert.assertEquals(users.size(), 9);
        User user = users.get(0);
        Assert.assertEquals(user.getId().longValue(), 2L);
        Assert.assertEquals(user.getName(), "B");
        Assert.assertEquals(user.getAge().intValue(), 12);
        Assert.assertEquals(user.getLocation(), "江苏省徐州市");
        Assert.assertEquals(user.getStatus(), Status.AVAILABLE);
        Assert.assertNull(user.getFeature());
        Assert.assertNull(user.getUpdateTime());
    }

    @Test
    public void testEmptyColumn() {
        User user = userMapper.selectByPrimaryKeyWithColumn(2L);
        Assert.assertNull(user);

        List<User> users1 = userMapper.selectByExampleWithColumn(new UserExample());
        Assert.assertEquals(10, users1.size());
        Assert.assertNull(users1.get(0));
        Assert.assertNull(users1.get(9));

        List<User> users2 = userMapper.selectByExampleWithColumn(new UserExample(), Column.excludes(Column.ID, Column.NAME, Column.AGE, Column.LOCATION, Column.STATUS, Column.CREATE_TIME, Column.UPDATE_TIME, Column.FEATURE));
        Assert.assertEquals(10, users2.size());
        Assert.assertNull(users2.get(0));
        Assert.assertNull(users2.get(9));
    }

    @Test
    public void testWithDistinct() {
        UserExample example = new UserExample().distinct(true);
        List<User> users = userMapper.selectByExampleWithColumn(example, Column.LOCATION);
        Assert.assertEquals(users.size(), 3);
        Set<String> locations = users.stream().map(User::getLocation).collect(Collectors.toSet());
        Set<String> exceptLocations = new HashSet<>();
        exceptLocations.add("江苏省南京市");
        exceptLocations.add("江苏省徐州市");
        exceptLocations.add("江苏省盐城市");
        Assert.assertTrue(exceptLocations.containsAll(locations));
    }

}
