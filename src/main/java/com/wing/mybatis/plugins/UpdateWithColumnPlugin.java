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
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 依赖列枚举的更新方法插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class UpdateWithColumnPlugin extends BasePlugin {
    private static final String METHOD_UPDATE_BY_PRIMARY_KEY_WITH_COLUMN = "updateByPrimaryKeyWithColumn";
    private static final String METHOD_UPDATE_BY_EXAMPLE_WITH_COLUMN = "updateByExampleWithColumn";

    @Override
    public boolean validate(List<String> warnings) {
        return super.validate(warnings) && dependentPluginCheck(ModelColumnPlugin.class);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final Parameter record = new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record", "@Param(\"record\")");
        final Parameter columnParam = new Parameter(JavaElementGenerator.getColumnEnumType(introspectedTable), "columns", "@Param(\"columns\")", true);

        //updateByPrimaryKeyWithColumn
        final Method updateByPrimaryKeySelectiveWithColumnMethod = JavaElementGenerator.generateMethod(
            METHOD_UPDATE_BY_PRIMARY_KEY_WITH_COLUMN,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            record,
            columnParam
        );
        commentGenerator.addGeneralMethodComment(updateByPrimaryKeySelectiveWithColumnMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, updateByPrimaryKeySelectiveWithColumnMethod);

        //updateByExampleWithColumn
        final Method updateByExampleSelectiveWithColumnMethod = JavaElementGenerator.generateMethod(
            METHOD_UPDATE_BY_EXAMPLE_WITH_COLUMN,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            record,
            new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example", "@Param(\"example\")"),
            columnParam
        );
        commentGenerator.addGeneralMethodComment(updateByExampleSelectiveWithColumnMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, updateByExampleSelectiveWithColumnMethod);

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        //updateByPrimaryKeyWithColumn
        final XmlElement updateByPrimaryKeySelectiveElement = this.generateUpdateByPrimaryKeyWithColumn(introspectedTable);
        commentGenerator.addComment(updateByPrimaryKeySelectiveElement);
        document.getRootElement().addElement(updateByPrimaryKeySelectiveElement);

        //updateByExampleWithColumn
        final XmlElement updateByExampleSelectiveElement = this.generateUpdateByExampleWithColumn(introspectedTable);
        commentGenerator.addComment(updateByExampleSelectiveElement);
        document.getRootElement().addElement(updateByExampleSelectiveElement);

        return true;
    }

    private XmlElement generateUpdateByPrimaryKeyWithColumn(IntrospectedTable introspectedTable) {
        final XmlElement element = new XmlElement("update");
        element.addAttribute(new Attribute("id", METHOD_UPDATE_BY_PRIMARY_KEY_WITH_COLUMN));
        element.addAttribute(new Attribute("parameterType", "map"));

        element.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        element.addElement(new TextElement("set"));
        element.addElement(this.generateSetSelective(introspectedTable.getNonPrimaryKeyColumns()));

        element.addElement(XmlElementGenerator.generateWhereByPrimaryKeyTo(introspectedTable.getPrimaryKeyColumns(), "record."));
        return element;
    }

    private XmlElement generateUpdateByExampleWithColumn(IntrospectedTable introspectedTable) {
        final XmlElement element = new XmlElement("update");
        element.addAttribute(new Attribute("id", METHOD_UPDATE_BY_EXAMPLE_WITH_COLUMN));
        element.addAttribute(new Attribute("parameterType", "map"));

        element.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        element.addElement(new TextElement("set"));
        element.addElement(this.generateSetSelective(introspectedTable.getAllColumns()));

        element.addElement(XmlElementGenerator.getUpdateByExampleIncludeElement(introspectedTable));
        return element;
    }

    /**
     * sets selective
     *
     * @param columns
     * @return
     */
    private Element generateSetSelective(List<IntrospectedColumn> columns) {
        //普通节点
        final TextElement normalElement = new TextElement("${column.delimitedColumnName} = #{record.${column.javaProperty},jdbcType=${column.jdbcType}}");

        //typeHandler 节点
        final List<XmlElement> typeHandlerElements = new ArrayList<>();
        for (IntrospectedColumn column : columns) {
            if (StringUtility.stringHasValue(column.getTypeHandler())) {
                XmlElement whenEle = new XmlElement("when");
                whenEle.addAttribute(new Attribute("test", "'" + column.getActualColumnName() + "' == column.column"));
                whenEle.addElement(new TextElement("${column.delimitedColumnName} = " + XmlElementGenerator.getParameterClauseWithColumn("record.${column.javaProperty}", column)));
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
