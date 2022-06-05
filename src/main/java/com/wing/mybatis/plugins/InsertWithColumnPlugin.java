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
 * 依赖列枚举的插入方法插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class InsertWithColumnPlugin extends BasePlugin {
    private static final String METHOD_INSERT_SELECTIVE_WITH_COLUMN = "insertWithColumn";

    @Override
    public boolean validate(List<String> warnings) {
        return super.validate(warnings) && dependentPluginCheck(ModelColumnPlugin.class);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final Method insertSelectiveWithColumnMethod = JavaElementGenerator.generateMethod(
            METHOD_INSERT_SELECTIVE_WITH_COLUMN,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record", "@Param(\"record\")"),
            new Parameter(JavaElementGenerator.getColumnEnumType(introspectedTable), "columns", "@Param(\"columns\")", true)
        );
        commentGenerator.addGeneralMethodComment(insertSelectiveWithColumnMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, insertSelectiveWithColumnMethod);
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        final XmlElement insertSelectiveElement = this.generateInsertSelectiveElement(introspectedTable);
        commentGenerator.addComment(insertSelectiveElement);
        document.getRootElement().addElement(insertSelectiveElement);
        return true;
    }

    private XmlElement generateInsertSelectiveElement(IntrospectedTable introspectedTable) {
        final XmlElement element = new XmlElement("insert");
        element.addAttribute(new Attribute("id", METHOD_INSERT_SELECTIVE_WITH_COLUMN));
        element.addAttribute(new Attribute("parameterType", "map"));

        if (introspectedTable.getGeneratedKey() != null) {
            XmlElementGenerator.useGeneratedKeys(element, introspectedTable, "record.");
        }
        element.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));
        element.addElement(XmlElementGenerator.getSelectiveElement(new TextElement("${column.delimitedColumnName}")));
        element.addElement(new TextElement(" ) values ( "));
        element.addElement(this.generateValueSelective(introspectedTable.getAllColumns()));
        element.addElement(new TextElement(" )"));
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
