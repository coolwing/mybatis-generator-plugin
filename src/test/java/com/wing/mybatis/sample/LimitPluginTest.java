package com.wing.mybatis.sample;

import java.util.List;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LimitPluginTest extends BasePluginTest {

    @BeforeClass
    public static void reset() throws Exception {
        BasePluginTest.resetDB();
    }

    @Test
    public void testLimit() {
        UserExample example = new UserExample();
        example.limit(2);
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
        Assert.assertEquals(users.get(1).getId().longValue(), 2L);
    }

    @Test
    public void testLimit2() {
        UserExample example = new UserExample();
        example.limit(2, 2);
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getId().longValue(), 3L);
        Assert.assertEquals(users.get(1).getId().longValue(), 4L);
    }

    @Test
    public void testPage() {
        UserExample example = new UserExample();
        example.page(3, 2);
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getId().longValue(), 5L);
        Assert.assertEquals(users.get(1).getId().longValue(), 6L);
    }

}
