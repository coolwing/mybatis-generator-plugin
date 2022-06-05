package com.wing.mybatis.sample;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.sample.common.Status;
import org.junit.Assert;
import org.junit.Test;

public class LombokPluginTest extends BasePluginTest {

    @Test
    public void testGetAndSet() {
        User user = new User();
        user.setName("lombok test");
        Assert.assertEquals(user.getName(), "lombok test");
    }

    @Test
    public void testAllArgsConstructor() {
        Date date = new Date();
        User user = new User(10L, "all", 10, "江苏省", Status.AVAILABLE, date, date, "blob".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(user.getId().longValue(), 10L);
        Assert.assertEquals(user.getName(), "all");
        Assert.assertEquals(user.getAge().intValue(), 10);
        Assert.assertEquals(user.getLocation(), "江苏省");
        Assert.assertEquals(user.getStatus(), Status.AVAILABLE);
        Assert.assertEquals(user.getCreateTime(), date);
        Assert.assertEquals(user.getUpdateTime(), date);
        Assert.assertEquals(new String(user.getFeature()), "blob");
    }

    @Test
    public void testBuilder() {
        Date date = new Date();
        User user = User.builder().id(10L).name("all").age(10).location("江苏省").status(Status.AVAILABLE).createTime(date).updateTime(date).feature("blob".getBytes(StandardCharsets.UTF_8)).build();
        Assert.assertEquals(user.getId().longValue(), 10L);
        Assert.assertEquals(user.getName(), "all");
        Assert.assertEquals(user.getAge().intValue(), 10);
        Assert.assertEquals(user.getLocation(), "江苏省");
        Assert.assertEquals(user.getStatus(), Status.AVAILABLE);
        Assert.assertEquals(user.getCreateTime(), date);
        Assert.assertEquals(user.getUpdateTime(), date);
        Assert.assertEquals(new String(user.getFeature()), "blob");
    }
}
