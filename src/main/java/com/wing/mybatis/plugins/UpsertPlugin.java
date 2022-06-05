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
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 插入或更新插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class UpsertPlugin extends BasePlugin {
    public static final String METHOD_UPSERT = "upsert";
    public static final String METHOD_UPSERT_WITH_BLOBS = "upsertWithBLOBs";
    public static final String METHOD_UPSERT_SELECTIVE = "upsertSelective";

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!hasIdentityAndGeneratedAlwaysColumn(introspectedTable)) {
            return true;
        }
        final Method upsertMethod = JavaElementGenerator.generateMethod(
            METHOD_UPSERT,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            new Parameter(JavaElementGenerator.getModelTypeWithoutBLOBs(introspectedTable), "record")
        );
        commentGenerator.addGeneralMethodComment(upsertMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, upsertMethod);

        if (introspectedTable.hasBLOBColumns()) {
            final Method upsertWithBLOBsMethod = JavaElementGenerator.generateMethod(
                METHOD_UPSERT_WITH_BLOBS,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(JavaElementGenerator.getModelTypeWithoutBLOBs(introspectedTable), "record")
            );
            commentGenerator.addGeneralMethodComment(upsertWithBLOBsMethod, introspectedTable);
            FormatTools.addMethodWithBestPosition(interfaze, upsertWithBLOBsMethod);
        }

        final Method upsertSelectiveMethod = JavaElementGenerator.generateMethod(
            METHOD_UPSERT_SELECTIVE,
            JavaVisibility.DEFAULT,
            FullyQualifiedJavaType.getIntInstance(),
            new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record")
        );
        commentGenerator.addGeneralMethodComment(upsertSelectiveMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(interfaze, upsertSelectiveMethod);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (!hasIdentityAndGeneratedAlwaysColumn(introspectedTable)) {
            return true;
        }
        final XmlElement upsertElement = this.generateUpsertElement(introspectedTable, METHOD_UPSERT, introspectedTable.getNonBLOBColumns(), false);
        commentGenerator.addComment(upsertElement);
        document.getRootElement().addElement(upsertElement);
        if (introspectedTable.hasBLOBColumns()) {
            final XmlElement upsertWithBLOBsElement = this.generateUpsertElement(introspectedTable, METHOD_UPSERT_WITH_BLOBS, introspectedTable.getAllColumns(), false);
            commentGenerator.addComment(upsertWithBLOBsElement);
            document.getRootElement().addElement(upsertWithBLOBsElement);
        }
        final XmlElement upsertWithColumnElement = this.generateUpsertElement(introspectedTable, METHOD_UPSERT_SELECTIVE, introspectedTable.getAllColumns(), true);
        commentGenerator.addComment(upsertWithColumnElement);
        document.getRootElement().addElement(upsertWithColumnElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private boolean hasIdentityAndGeneratedAlwaysColumn(IntrospectedTable introspectedTable) {
        final List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (IntrospectedColumn ic : columns) {
            if (ic.isGeneratedAlways() || ic.isIdentity()) {
                return true;
            }
        }
        return false;
    }

    private XmlElement generateUpsertElement(IntrospectedTable introspectedTable, String methodName, List<IntrospectedColumn> introspectedColumns, boolean isSelective) {
        final List<IntrospectedColumn> columns = ListUtilities.removeGeneratedAlwaysColumns(introspectedColumns);
        final XmlElement insertElement = new XmlElement("insert");
        insertElement.addAttribute(new Attribute("id", methodName));
        insertElement.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
        XmlElementGenerator.useGeneratedKeys(insertElement, introspectedTable, null);

        insertElement.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));
        insertElement.addElement(this.generateUpsertCommonColumns(columns, isSelective, 1));
        insertElement.addElement(new TextElement(") values ("));
        insertElement.addElement(this.generateUpsertCommonColumns(columns, isSelective, 2));
        insertElement.addElement(new TextElement(") on duplicate key update "));
        insertElement.addElement(this.generateUpsertCommonColumns(columns, isSelective, 3));
        return insertElement;
    }

    private Element generateUpsertCommonColumns(List<IntrospectedColumn> columns, boolean isSelective, int type) {
        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("suffixOverrides", ","));
        for (IntrospectedColumn introspectedColumn : columns) {
            Element columnElement = generateSelectiveCommColumnTo(introspectedColumn, type);
            if (isSelective || introspectedColumn.isGeneratedAlways() || introspectedColumn.isIdentity()) {
                XmlElement ifElement = new XmlElement("if");
                ifElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty() + " != null"));
                ifElement.addElement(columnElement);
                trimElement.addElement(ifElement);
            } else {
                trimElement.addElement(columnElement);
            }
        }
        return trimElement;
    }

    private Element generateSelectiveCommColumnTo(IntrospectedColumn introspectedColumn, int type) {
        switch (type) {
            case 3:
                return new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " = " + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, null) + ",");
            case 2:
                return new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, null) + ",");
            case 1:
                return new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + ",");
            default:
                break;
        }
        return null;
    }
}