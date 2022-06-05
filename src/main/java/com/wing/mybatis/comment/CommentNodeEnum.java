package com.wing.mybatis.comment;

/**
 * 注释模版配置节点
 *
 * @author wing
 * @date 2022/5/26
 **/
public enum CommentNodeEnum {
    /**
     * xml root 节点注释
     */
    ADD_ROOT_COMMENT("addRootComment"),
    /**
     * Xml 节点注释
     */
    ADD_COMMENT("addComment"),
    /**
     * java 文件注释
     */
    ADD_JAVA_FILE_COMMENT("addJavaFileComment"),
    /**
     * 接口 注释
     */
    ADD_INTERFACE_COMMENT("addInterfaceComment"),
    /**
     * 类 注释
     */
    ADD_CLASS_COMMENT("addClassComment"),
    /**
     * model 类文件注释
     */
    ADD_MODEL_CLASS_COMMENT("addModelClassComment"),
    /**
     * 字段 注释
     */
    ADD_FIELD_COMMENT("addFieldComment"),
    /**
     * setter 方法注释
     */
    ADD_SETTER_COMMENT("addSetterComment"),
    /**
     * getter 方式注释
     */
    ADD_GETTER_COMMENT("addGetterComment"),
    /**
     * java 方法注释
     */
    ADD_GENERAL_METHOD_COMMENT("addGeneralMethodComment"),
    /**
     * 枚举 注释
     */
    ADD_ENUM_COMMENT("addEnumComment"),
    ;

    private final String value;

    CommentNodeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
