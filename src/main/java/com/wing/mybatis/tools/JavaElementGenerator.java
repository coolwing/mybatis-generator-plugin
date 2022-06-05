package com.wing.mybatis.tools;

import com.wing.mybatis.plugins.ModelColumnPlugin;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

/**
 * java元素生成工具
 *
 * @author wing
 * @date 2022/5/26
 **/
public class JavaElementGenerator {

    public static Field generatePrivateStringField(String fieldName) {
        return generateField(fieldName, JavaVisibility.PRIVATE, FullyQualifiedJavaType.getStringInstance(), null);
    }

    /**
     * 生成属性
     *
     * @param fieldName  常量名称
     * @param visibility 可见性
     * @param javaType   类型
     * @param initString 初始化字段
     * @return
     */
    public static Field generateField(String fieldName, JavaVisibility visibility, FullyQualifiedJavaType javaType, String initString) {
        Field field = new Field(fieldName, javaType);
        field.setVisibility(visibility);
        field.setInitializationString(initString);
        return field;
    }

    /**
     * 生成构造方法
     *
     * @param name
     * @param parameters
     * @return
     */
    public static Method generateConstructorMethod(String name, Parameter... parameters) {
        Method constructor = new Method(name);
        constructor.setConstructor(true);
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                constructor.addParameter(parameter);
            }
        }
        return constructor;
    }

    /**
     * 生成方法
     *
     * @param methodName 方法名
     * @param visibility 可见性
     * @param returnType 返回值类型
     * @param parameters 参数列表
     * @return
     */
    public static Method generateMethod(String methodName, JavaVisibility visibility, FullyQualifiedJavaType returnType, Parameter... parameters) {
        Method method = new Method(methodName);
        method.setVisibility(visibility);
        method.setReturnType(returnType);
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                method.addParameter(parameter);
            }
        }

        return method;
    }

    /**
     * 生成方法实现体
     *
     * @param method    方法
     * @param bodyLines 方法实现行
     * @return
     */
    public static Method appendMethodBody(Method method, String... bodyLines) {
        if (bodyLines != null) {
            for (String bodyLine : bodyLines) {
                method.addBodyLine(bodyLine);
            }
        }
        return method;
    }

    /**
     * 生成Filed的Set方法
     *
     * @param field field
     * @return
     */
    public static Method generateSetterMethod(Field field) {
        Method method = generateMethod(
            "set" + FormatTools.upFirstChar(field.getName()),
            JavaVisibility.PUBLIC,
            null,
            new Parameter(field.getType(), field.getName())
        );
        return appendMethodBody(method, "this." + field.getName() + " = " + field.getName() + ";");
    }

    /**
     * 生成Filed的Get方法
     *
     * @param field field
     * @return
     */
    public static Method generateGetterMethod(Field field) {
        Method method = generateMethod(
            "get" + FormatTools.upFirstChar(field.getName()),
            JavaVisibility.PUBLIC,
            field.getType()
        );
        return appendMethodBody(method, "return this." + field.getName() + ";");
    }

    /**
     * 获取Model没有BLOBs类时的类型
     *
     * @param introspectedTable
     * @return
     */
    public static FullyQualifiedJavaType getModelTypeWithoutBLOBs(IntrospectedTable introspectedTable) {
        return new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
    }

    /**
     * 获取Model有BLOBs类时的类型
     *
     * @param introspectedTable
     * @return
     */
    public static FullyQualifiedJavaType getModelTypeWithBLOBs(IntrospectedTable introspectedTable) {
        return new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
    }

    /**
     * 获取column枚举的名称
     *
     * @param introspectedTable
     * @return
     */
    public static FullyQualifiedJavaType getColumnEnumType(IntrospectedTable introspectedTable) {
        final String[] split = introspectedTable.getExampleType().split("\\.");
        return new FullyQualifiedJavaType(split[split.length - 1] + "." + ModelColumnPlugin.ENUM_COLUMN);
    }
}
