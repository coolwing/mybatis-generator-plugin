package com.wing.mybatis.sample;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.sample.common.Status;
import org.junit.Assert;
import org.junit.Test;

public class ModelCloneablePluginTest extends BasePluginTest {
    @Test
    public void testClone() throws Exception {
        Date date = new Date();
        User user = new User(10L, "all", 10, "江苏省", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));
        User cloneUser = user.clone();
        Assert.assertEquals(cloneUser.getId().longValue(), 10L);
        Assert.assertEquals(cloneUser.getName(), "all");
        Assert.assertEquals(cloneUser.getAge().intValue(), 10);
        Assert.assertEquals(cloneUser.getLocation(), "江苏省");
        Assert.assertEquals(cloneUser.getStatus(), Status.AVAILABLE);
        Assert.assertEquals(cloneUser.getCreateTime(), date);
        Assert.assertEquals(cloneUser.getUpdateTime(), date);
        Assert.assertEquals(new String(cloneUser.getFeature()), "blob");

        user.getCreateTime().setTime(1609430400000L);
        Assert.assertEquals(cloneUser.getCreateTime().getTime(), 1609430400000L);
    }

    @Test
    public void testDeepClone() throws Exception {
        Date date = new Date();
        User user = new User(10L, "all", 10, "江苏省", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));

        User deepCloneUser = user.deepClone();
        Assert.assertEquals(deepCloneUser.getId().longValue(), 10L);
        Assert.assertEquals(deepCloneUser.getName(), "all");
        Assert.assertEquals(deepCloneUser.getAge().intValue(), 10);
        Assert.assertEquals(deepCloneUser.getLocation(), "江苏省");
        Assert.assertEquals(deepCloneUser.getStatus(), Status.AVAILABLE);
        Assert.assertEquals(deepCloneUser.getCreateTime(), date);
        Assert.assertEquals(deepCloneUser.getUpdateTime(), date);
        Assert.assertEquals(new String(deepCloneUser.getFeature()), "blob");

        user.getCreateTime().setTime(1609430400000L);
        Assert.assertNotEquals(deepCloneUser.getCreateTime().getTime(), date.getTime());
    }
}
