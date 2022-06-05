package com.wing.mybatis.plugins;

import java.util.List;

import com.wing.mybatis.tools.FormatTools;
import com.wing.mybatis.tools.JavaElementGenerator;
import com.wing.mybatis.tools.XmlElementGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 依赖列枚举的查询方法插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class SelectWithColumnPlugin extends BasePlugin {
    public static final String METHOD_SELECT_BY_EXAMPLE_WITH_COLUMN = "selectByExampleWithColumn";
    public static final String METHOD_SELECT_BY_PRIMARY_KEY_WITH_COLUMN = "selectByPrimaryKeyWithColumn";

    @Override
    public boolean validate(List<String> warnings) {
        return super.validate(warnings) && dependentPluginCheck(ModelColumnPlugin.class);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        final Method selectByExampleMethod = JavaElementGenerator.generateMethod(
            METHOD_SELECT_BY_EXAMPLE_WITH_COLUMN,
            JavaVisibility.DEFAULT,
            listInstance,
            new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"),
            new Parameter(JavaElementGenerator.getColumnEnumType(introspectedTable), "columns", "@Param(\"columns\")", true)
        );
        commentGenerator.addGeneralMethodComment(selectByExampleMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, selectByExampleMethod);

        final Method selectByKeyMethod = JavaElementGenerator.generateMethod(
            METHOD_SELECT_BY_PRIMARY_KEY_WITH_COLUMN,
            JavaVisibility.DEFAULT,
            new FullyQualifiedJavaType(introspectedTable.getBaseRecordType())
        );
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            selectByKeyMethod.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty(), "@Param(\"" + introspectedColumn.getJavaProperty() + "\")"));
        }
        selectByKeyMethod.addParameter(new Parameter(JavaElementGenerator.getColumnEnumType(introspectedTable), "columns", "@Param(\"columns\")", true));
        commentGenerator.addGeneralMethodComment(selectByKeyMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, selectByKeyMethod);

        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        //1.selectByExampleWithColumn方法
        final XmlElement selectByExampleSelectiveElement = this.generateSelectSelectiveElement(METHOD_SELECT_BY_EXAMPLE_WITH_COLUMN, introspectedTable, true);
        commentGenerator.addComment(selectByExampleSelectiveElement);
        FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleSelectiveElement);

        //2.selectByPrimaryKeyWithColumn方法
        final XmlElement selectByPrimaryKeySelectiveElement = this.generateSelectSelectiveElement(METHOD_SELECT_BY_PRIMARY_KEY_WITH_COLUMN, introspectedTable, false);
        commentGenerator.addComment(selectByPrimaryKeySelectiveElement);
        FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKeySelectiveElement);
        return true;
    }

    private XmlElement generateSelectSelectiveElement(String id, IntrospectedTable introspectedTable, boolean byExample) {
        final XmlElement element = new XmlElement("select");
        element.addAttribute(new Attribute("id", id));
        if (introspectedTable.hasBLOBColumns()) {
            element.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
        } else {
            element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        }
        element.addAttribute(new Attribute("parameterType", "map"));

        element.addElement(new TextElement("select"));
        if (byExample) {
            final Element distinctByExampleElement = XmlElementGenerator.getDistinctByExampleElement();
            element.addElement(distinctByExampleElement);
        }

        final XmlElement selectiveWithColumnElement = XmlElementGenerator.getSelectiveElement(new TextElement("${column.delimitedColumnName}"));
        XmlElement otherwiseElement = new XmlElement("otherwise");
        otherwiseElement.addElement(new TextElement("null"));
        selectiveWithColumnElement.addElement(otherwiseElement);
        element.addElement(selectiveWithColumnElement);

        element.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

        if (byExample) {
            final Element updateByExampleIncludeElement = XmlElementGenerator.getUpdateByExampleIncludeElement(introspectedTable);
            element.addElement(updateByExampleIncludeElement);
            final Element orderByExampleElement = XmlElementGenerator.getOrderByExampleElement();
            element.addElement(orderByExampleElement);
            //关联LimitPlugin
            if (dependentPluginCheck(LimitPlugin.class)) {
                final Element limitRefElement = LimitPlugin.getLimitElement(true);
                element.addElement(limitRefElement);
            }
        } else {
            final TextElement textElement = XmlElementGenerator.generateWhereByPrimaryKeyTo(introspectedTable.getPrimaryKeyColumns(), null);
            element.addElement(textElement);
        }
        return element;
    }

}
