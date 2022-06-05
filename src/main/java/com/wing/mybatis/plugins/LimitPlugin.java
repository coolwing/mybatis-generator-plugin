package com.wing.mybatis.plugins;

import com.wing.mybatis.tools.FormatTools;
import com.wing.mybatis.tools.JavaElementGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * Example支持分页的插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class LimitPlugin extends BasePlugin {

    private static final String METHOD_LIMIT = "limit";
    private static final String METHOD_PAGE = "page";
    private static final String FIELD_OFFSET = "offset";
    private static final String FIELD_ROWS = "rows";

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final FullyQualifiedJavaType intInstance = FullyQualifiedJavaType.getIntInstance();
        final PrimitiveTypeWrapper integerWrapper = intInstance.getPrimitiveTypeWrapper();
        // 添加offset和rows字段
        final Field offsetField = JavaElementGenerator.generateField(
            FIELD_OFFSET,
            JavaVisibility.PROTECTED,
            integerWrapper,
            null
        );
        commentGenerator.addFieldComment(offsetField, introspectedTable);
        topLevelClass.addField(offsetField);

        final Field rowsField = JavaElementGenerator.generateField(
            FIELD_ROWS,
            JavaVisibility.PROTECTED,
            integerWrapper,
            null
        );
        commentGenerator.addFieldComment(rowsField, introspectedTable);
        topLevelClass.addField(rowsField);

        // 增加getter && setter 方法
        final Method setOffsetMethod = JavaElementGenerator.generateSetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(setOffsetMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, setOffsetMethod);

        final Method getOffsetMethod = JavaElementGenerator.generateGetterMethod(offsetField);
        commentGenerator.addGeneralMethodComment(getOffsetMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, getOffsetMethod);

        final Method setRowsMethod = JavaElementGenerator.generateSetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(setRowsMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, setRowsMethod);

        final Method getRowsMethod = JavaElementGenerator.generateGetterMethod(rowsField);
        commentGenerator.addGeneralMethodComment(getRowsMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(topLevelClass, getRowsMethod);

        //提供快捷方法
        final Method simpleLimitMethod = JavaElementGenerator.generateMethod(
            METHOD_LIMIT,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(intInstance, FIELD_ROWS)
        );
        commentGenerator.addGeneralMethodComment(simpleLimitMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(simpleLimitMethod,
            "this." + FIELD_ROWS + " = " + FIELD_ROWS + ";",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, simpleLimitMethod);

        final Method limitMethod = JavaElementGenerator.generateMethod(
            METHOD_LIMIT,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(intInstance, FIELD_OFFSET),
            new Parameter(intInstance, FIELD_ROWS)
        );
        commentGenerator.addGeneralMethodComment(limitMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(limitMethod,
            "this." + FIELD_OFFSET + " = " + FIELD_OFFSET + ";",
            "this." + FIELD_ROWS + " = " + FIELD_ROWS + ";",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, limitMethod);

        final Method pageMethod = JavaElementGenerator.generateMethod(
            METHOD_PAGE,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(intInstance, "page"),
            new Parameter(intInstance, "pageSize")
        );
        commentGenerator.addGeneralMethodComment(pageMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(pageMethod,
            "this." + FIELD_OFFSET + " = " + "(page - 1)" + " * pageSize;",
            "this." + FIELD_ROWS + " = pageSize;",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, pageMethod);

        //clear 方法增加 offset 和 rows的清理
        final Method clearMethod = topLevelClass.getMethods().stream().filter(method -> "clear".equals(method.getName())).findFirst().orElse(null);
        if (clearMethod != null) {
            clearMethod.addBodyLine(FIELD_ROWS + " = null;");
            clearMethod.addBodyLine(FIELD_OFFSET + " = null;");
        }

        return true;
    }

    private static Element generateLimitElement(boolean exampleBased) {
        final String prefix = exampleBased ? "example." : "";

        final XmlElement limitElement = new XmlElement("if");
        limitElement.addAttribute(new Attribute("test", prefix + FIELD_ROWS + " != null"));

        final XmlElement ifOffsetNotNullElement = new XmlElement("if");
        ifOffsetNotNullElement.addAttribute(new Attribute("test", prefix + FIELD_OFFSET + " != null"));
        ifOffsetNotNullElement.addElement(new TextElement("limit ${" + prefix + FIELD_OFFSET + "}, ${" + prefix + FIELD_ROWS + "}"));
        limitElement.addElement(ifOffsetNotNullElement);

        final XmlElement ifOffsetNullElement = new XmlElement("if");
        ifOffsetNullElement.addAttribute(new Attribute("test", prefix + FIELD_OFFSET + " == null"));
        ifOffsetNullElement.addElement(new TextElement("limit ${" + prefix + FIELD_ROWS + "}"));
        limitElement.addElement(ifOffsetNullElement);
        if (exampleBased) {
            final XmlElement sqlElement = new XmlElement("if");
            sqlElement.addAttribute(new Attribute("test", "example != null"));
            sqlElement.addElement(limitElement);
            return sqlElement;
        } else {
            return limitElement;
        }
    }

    public static Element getLimitElement(boolean exampleBased) {
        return generateLimitElement(exampleBased);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        element.addElement(generateLimitElement(false));
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        element.addElement(generateLimitElement(false));
        return true;
    }

}
