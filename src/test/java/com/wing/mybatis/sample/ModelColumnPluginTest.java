package com.wing.mybatis.sample;

import com.wing.mybatis.product.example.UserExample.Column;
import org.junit.Assert;
import org.junit.Test;

public class ModelColumnPluginTest extends BasePluginTest {

    @Test
    public void test1() {
        Column id = Column.ID;
        Assert.assertEquals(id.getColumn(), "id");
        Assert.assertEquals(id.getJavaProperty(), "id");
        Assert.assertEquals(id.getJdbcType(), "BIGINT");
        Assert.assertEquals(id.getDelimitedColumnName(), "id");
    }

    @Test
    public void test2() {
        Column name = Column.NAME;
        Assert.assertEquals(name.getColumn(), "name");
        Assert.assertEquals(name.getJavaProperty(), "name");
        Assert.assertEquals(name.getJdbcType(), "VARCHAR");
        Assert.assertEquals(name.getDelimitedColumnName(), "`name`");
    }

    @Test
    public void test3() {
        Assert.assertEquals(Column.LOCATION.getColumn(), "address");
        Assert.assertEquals(Column.LOCATION.getJavaProperty(), "location");
    }

    @Test
    public void test4() {
        Assert.assertEquals(Column.ofColumn("status"), Column.STATUS);
    }

    @Test
    public void test5() {
        Column[] columns = Column.excludes(Column.FEATURE);
        Assert.assertEquals(columns.length, 7);
    }

}
