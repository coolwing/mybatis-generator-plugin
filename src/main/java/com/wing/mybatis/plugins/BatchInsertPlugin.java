package com.wing.mybatis.plugins;

import java.util.ArrayList;
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
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 批量插入插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class BatchInsertPlugin extends BasePlugin {
    public static final String METHOD_BATCH_INSERT = "batchInsert";
    public static final String METHOD_BATCH_INSERT_WITH_COLUMN = "batchInsertWithColumn";

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //1.batchInsert
        final FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());
        final Method batchInsertMethod = JavaElementGenerator.generateMethod(
            METHOD_BATCH_INSERT,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            new Parameter(listType, "list", "@Param(\"list\")")

        );
        commentGenerator.addGeneralMethodComment(batchInsertMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, batchInsertMethod);

        //2.batchInsertWithColumn
        if (dependentPluginCheck(ModelColumnPlugin.class)) {
            final Method batchInsertWithColumn = JavaElementGenerator.generateMethod(
                METHOD_BATCH_INSERT_WITH_COLUMN,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")"),
                new Parameter(JavaElementGenerator.getColumnEnumType(introspectedTable), "columns", "@Param(\"columns\")", true)
            );
            commentGenerator.addGeneralMethodComment(batchInsertWithColumn, introspectedTable);
            FormatTools.addMethodWithBestPosition(interfaze, batchInsertWithColumn);
        }
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // 1. batchInsert
        final XmlElement batchInsertElement = this.generateBatchInsertElement(introspectedTable);
        commentGenerator.addComment(batchInsertElement);
        document.getRootElement().addElement(batchInsertElement);

        //2.batchInsertWithColumn
        if (dependentPluginCheck(ModelColumnPlugin.class)) {
            final XmlElement batchInsertWithColumnElement = this.generateBatchInsertWithColumnElement(introspectedTable);
            commentGenerator.addComment(batchInsertWithColumnElement);
            document.getRootElement().addElement(batchInsertWithColumnElement);
        }
        return true;
    }

    private XmlElement generateBatchInsertElement(IntrospectedTable introspectedTable) {
        final XmlElement insertElement = new XmlElement("insert");
        insertElement.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
        insertElement.addAttribute(new Attribute("parameterType", "map"));
        XmlElementGenerator.useGeneratedKeys(insertElement, introspectedTable, null);

        insertElement.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ( "));

        final Element keysElement = XmlElementGenerator.generateKeys(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), null);
        insertElement.addElement(keysElement);
        insertElement.addElement(new TextElement(" ) values "));

        final XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        final TextElement valuesElement = XmlElementGenerator.generateValues(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item.");
        foreachElement.addElement(valuesElement);

        insertElement.addElement(foreachElement);
        return insertElement;
    }

    private XmlElement generateBatchInsertWithColumnElement(IntrospectedTable introspectedTable) {
        final XmlElement element = new XmlElement("insert");
        element.addAttribute(new Attribute("id", METHOD_BATCH_INSERT_WITH_COLUMN));
        element.addAttribute(new Attribute("parameterType", "map"));
        XmlElementGenerator.useGeneratedKeys(element, introspectedTable, "list.");

        element.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));
        element.addElement(XmlElementGenerator.getSelectiveElement(new TextElement("${column.delimitedColumnName}")));
        element.addElement(new TextElement(" ) values "));

        final XmlElement eachValueElement = new XmlElement("foreach");
        eachValueElement.addAttribute(new Attribute("collection", "list"));
        eachValueElement.addAttribute(new Attribute("item", "record"));
        eachValueElement.addAttribute(new Attribute("separator", ","));
        eachValueElement.addElement(new TextElement("("));
        eachValueElement.addElement(this.generateValueSelective(introspectedTable.getAllColumns()));
        eachValueElement.addElement(new TextElement(")"));

        element.addElement(eachValueElement);
        return element;
    }

    private Element generateValueSelective(List<IntrospectedColumn> columns) {
        //普通节点
        final TextElement normalElement = new TextElement("#{record.${column.javaProperty},jdbcType=${column.jdbcType}}");

        //typeHandler 节点
        final List<XmlElement> typeHandlerElements = new ArrayList<>();
        for (IntrospectedColumn column : columns) {
            if (StringUtility.stringHasValue(column.getTypeHandler())) {
                XmlElement whenEle = new XmlElement("when");
                whenEle.addAttribute(new Attribute("test", "'" + column.getActualColumnName() + "' == column.column"));
                whenEle.addElement(new TextElement(XmlElementGenerator.getParameterClauseWithColumn("record.${column.javaProperty}", column)));
                typeHandlerElements.add(whenEle);
            }
        }

        if (typeHandlerElements.isEmpty()) {
            return XmlElementGenerator.getSelectiveElement(normalElement);
        } else {
            final XmlElement chooseElement = new XmlElement("choose");
            for (XmlElement whenElement : typeHandlerElements) {
                chooseElement.addElement(whenElement);
            }
            final XmlElement otherwiseElement = new XmlElement("otherwise");
            otherwiseElement.addElement(normalElement);
            chooseElement.addElement(otherwiseElement);
            return XmlElementGenerator.getSelectiveElement(chooseElement);
        }
    }
}