package com.wing.mybatis.plugins;

import java.util.Properties;

import com.wing.mybatis.tools.JavaElementGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * model类添加clone方法
 * 支持深拷贝方法，使用序列化实现
 *
 * @author wing
 * @date 2021/11/19
 **/
public class ModelCloneablePlugin extends BasePlugin {
    private static final String METHOD_CLONE = "clone";
    private static final String METHOD_DEEP_CLONE = "deepClone";
    private static final String PROPERTY_ENABLE_DEEP_CLONE = "enableDeepClone";
    private boolean enableDeepClone = false;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        final String enable = properties.getProperty(PROPERTY_ENABLE_DEEP_CLONE);
        this.enableDeepClone = enable != null && StringUtility.isTrue(enable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addCloneMethod(topLevelClass, introspectedTable);
        if (enableDeepClone) {
            this.addDeepCloneMethod(topLevelClass, introspectedTable);
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 支持Cloneable
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addCloneMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("java.lang.Cloneable"));
        final Method cloneMethod = JavaElementGenerator.generateMethod(
            METHOD_CLONE,
            JavaVisibility.PUBLIC,
            topLevelClass.getType()
        );
        commentGenerator.addGeneralMethodComment(cloneMethod, introspectedTable);
        cloneMethod.addAnnotation("@Override");
        cloneMethod.addException(new FullyQualifiedJavaType("java.lang.CloneNotSupportedException"));
        cloneMethod.addBodyLine("return (" + topLevelClass.getType().getShortName() + ") super.clone();");
        topLevelClass.addMethod(cloneMethod);
    }

    /**
     * 支持深拷贝（deepClone）
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addDeepCloneMethod(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.ByteArrayInputStream"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.ByteArrayOutputStream"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.ObjectInputStream"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.ObjectOutputStream"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.IOException"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.Serializable"));

        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("java.io.Serializable"));

        final Method deepCloneMethod = JavaElementGenerator.generateMethod(
            METHOD_DEEP_CLONE,
            JavaVisibility.PUBLIC,
            topLevelClass.getType()
        );

        deepCloneMethod.addException(new FullyQualifiedJavaType("java.io.IOException"));
        deepCloneMethod.addException(new FullyQualifiedJavaType("java.lang.ClassNotFoundException"));
        commentGenerator.addGeneralMethodComment(deepCloneMethod, introspectedTable);

        final String shortName = topLevelClass.getType().getShortName();
        JavaElementGenerator.appendMethodBody(deepCloneMethod,
            "ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();",
            "ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);",
            "objectOutputStream.writeObject(this);",
            "ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());",
            "ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);",
            shortName + " target = (" + shortName + ")objectInputStream.readObject();",
            "objectOutputStream.close();",
            "objectInputStream.close();",
            "return target;"
        );
        topLevelClass.addMethod(deepCloneMethod);
    }
}
