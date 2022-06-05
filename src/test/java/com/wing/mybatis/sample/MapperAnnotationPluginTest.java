package com.wing.mybatis.sample;

import com.wing.mybatis.product.mapper.UserMapper;
import org.apache.ibatis.annotations.Mapper;
import org.junit.Assert;
import org.junit.Test;

public class MapperAnnotationPluginTest extends BasePluginTest {

    @Test
    public void test() {
        Mapper annotation = UserMapper.class.getAnnotation(Mapper.class);
        Assert.assertNotNull(annotation);
    }
}
