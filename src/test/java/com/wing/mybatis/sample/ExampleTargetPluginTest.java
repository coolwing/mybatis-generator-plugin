package com.wing.mybatis.sample;

import com.wing.mybatis.product.example.UserExample;
import org.junit.Assert;
import org.junit.Test;

public class ExampleTargetPluginTest extends BasePluginTest {

    @Test
    public void test() {
        String name = UserExample.class.getName();
        Assert.assertEquals(name, "com.wing.mybatis.product.example." + UserExample.class.getSimpleName());
    }
}
