package com.wing.mybatis.comment;

import com.wing.mybatis.enhanced.InnerInterface;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 注释模版的参数名称
 *
 * @author wing
 * @date 2022/5/25
 **/
public enum ParameterEnum {
    /**
     * mgb标记
     */
    MGB("mgb", String.class, "mgb标记"),
    /**
     * 是否使用数据库注释
     */
    ENABLE_REMARK("enableRemark", Boolean.class, "是否使用数据库注释"),
    /**
     * 日期
     */
    DATE("date", String.class, "日期"),
    /**
     * XML元素
     */
    ELEMENT("element", XmlElement.class, "XML元素"),
    /**
     * JAVA文件
     */
    FILE("file", CompilationUnit.class, "JAVA文件"),
    /**
     * 接口
     */
    INNER_INTERFACE("innerInterface", InnerInterface.class, "接口"),
    /**
     * 类
     */
    CLASS("class", TopLevelClass.class, "类"),
    /**
     * 内部类
     */
    INNER_CLASS("innerClass", InnerClass.class, "内部类"),
    /**
     * 属性
     */
    FIELD("field", Field.class, "属性"),
    /**
     * 方法
     */
    METHOD("method", Method.class, "方法"),
    /**
     * 枚举
     */
    ENUM("enum", InnerEnum.class, "枚举"),
    /**
     * 表
     */
    TABLE("table", IntrospectedTable.class, "表"),
    /**
     * 列
     */
    COLUMN("column", IntrospectedColumn.class, "列"),
    ;
    private final String code;
    private final Class<?> clazz;
    private final String desc;

    ParameterEnum(String code, Class<?> clazz, String desc) {
        this.code = code;
        this.clazz = clazz;
        this.desc = desc;
    }

    public String code() {
        return this.code;
    }
}
