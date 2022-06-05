package com.wing.mybatis.sample;

import java.util.List;

import com.wing.mybatis.product.domain.User;
import com.wing.mybatis.product.example.UserExample;
import com.wing.mybatis.product.example.UserExample.SortType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExampleEnhancedPluginTest extends BasePluginTest {

    @BeforeClass
    public static void reset() throws Exception {
        BasePluginTest.resetDB();
    }

    @Test
    public void testOrderBy() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .orderBy("id", SortType.ASC);
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 10);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
        Assert.assertEquals(users.get(1).getId().longValue(), 2L);
    }

    @Test
    public void testOrderByWithPage() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .orderBy("id", SortType.DESC)
            .page(2, 2);
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getId().longValue(), 8L);
        Assert.assertEquals(users.get(1).getId().longValue(), 7L);
    }

    @Test
    public void testDistinct() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .distinct(true);
        Assert.assertTrue(example.isDistinct());
    }

    @Test
    public void testCriteriaWhen1() {
        UserExample example = UserExample.newAndCreateCriteria()
            .when(true, criteria -> criteria.andIdEqualTo(1L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
    }

    @Test
    public void testCriteriaWhen2() {
        UserExample example = UserExample.newAndCreateCriteria()
            .when(false, criteria -> criteria.andIdEqualTo(1L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 10);
    }

    @Test
    public void testCriteriaWhen3() {
        UserExample example = UserExample.newAndCreateCriteria()
            .when(true,
                criteria -> criteria.andIdEqualTo(1L),
                criteria -> criteria.andIdEqualTo(2L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
    }

    @Test
    public void testCriteriaWhen4() {
        UserExample example = UserExample.newAndCreateCriteria()
            .when(false,
                criteria -> criteria.andIdEqualTo(1L),
                criteria -> criteria.andIdEqualTo(2L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 2L);
    }

    @Test
    public void testExampleWhen1() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .when(true, e -> e.limit(1));
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
    }

    @Test
    public void testExampleWhen2() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .when(false, e -> e.limit(1));
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 10);
    }

    @Test
    public void testExampleWhen3() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .when(true,
                e -> e.limit(1),
                e -> e.limit(2));
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
    }

    @Test
    public void testExampleWhen4() {
        UserExample example = UserExample.newAndCreateCriteria()
            .example()
            .when(false,
                e -> e.limit(1),
                e -> e.limit(2));
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
        Assert.assertEquals(users.get(1).getId().longValue(), 2L);
    }

    @Test
    public void testCriteriaWhen11() {
        UserExample example = UserExample.newAndCreateCriteria()
            .andLocationEqualTo("江苏省南京市")
            .when(true,
                criteria -> criteria.andIdLessThanOrEqualTo(3L),
                criteria -> criteria.andIdGreaterThanOrEqualTo(3L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 1);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
    }

    @Test
    public void testCriteriaWhen12() {
        UserExample example = UserExample.newAndCreateCriteria()
            .andLocationEqualTo("江苏省南京市")
            .when(false,
                criteria -> criteria.andIdLessThanOrEqualTo(3L),
                criteria -> criteria.andIdGreaterThanOrEqualTo(3L))
            .example();
        List<User> users = userMapper.selectByExample(example);
        Assert.assertEquals(users.size(), 6);
        Assert.assertEquals(users.get(0).getId().longValue(), 5L);
    }

    @Test
    public void testExampleWhen11() {
        UserExample userExample = UserExample.newAndCreateCriteria()
            .andLocationEqualTo("江苏省南京市")
            .example()
            .when(true,
                example -> example.orderBy("id", SortType.ASC),
                example -> example.orderBy("id", SortType.DESC));
        List<User> users = userMapper.selectByExample(userExample);
        Assert.assertEquals(users.size(), 7);
        Assert.assertEquals(users.get(0).getId().longValue(), 1L);
        Assert.assertEquals(users.get(1).getId().longValue(), 5L);
    }

    @Test
    public void testExampleWhen12() {
        UserExample when = UserExample.newAndCreateCriteria()
            .andLocationEqualTo("江苏省南京市")
            .example()
            .when(false,
                example -> example.orderBy("id", SortType.ASC),
                example -> example.orderBy("id", SortType.DESC));
        List<User> users = userMapper.selectByExample(when);
        Assert.assertEquals(users.size(), 7);
        Assert.assertEquals(users.get(0).getId().longValue(), 10L);
        Assert.assertEquals(users.get(1).getId().longValue(), 9L);
    }

}
