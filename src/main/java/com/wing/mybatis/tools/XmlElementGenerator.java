package com.wing.mybatis.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Xml 节点生成工具
 *
 * @author wing
 * @date 2022/5/26
 **/
public class XmlElementGenerator {

    public static void useGeneratedKeys(XmlElement element, IntrospectedTable introspectedTable, String prefix) {
        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = IntrospectedTableTools.safeGetColumn(introspectedTable, gk.getColumn());
            if (introspectedColumn != null) {
                element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                element.addAttribute(new Attribute("keyProperty", (prefix == null ? "" : prefix) + introspectedColumn.getJavaProperty()));
                element.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
            }
        }
    }

    /**
     * <include refid="Base_Column_List" />
     *
     * @param introspectedTable
     * @return
     */
    public static Element getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
        return answer;
    }

    /**
     * <include refid="Blob_Column_List" />
     *
     * @param introspectedTable
     * @return
     */
    public static Element getBlobColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include");
        answer.addAttribute(new Attribute("refid", introspectedTable.getBlobColumnListId()));
        return answer;
    }

    /**
     * <if test="_parameter != null">
     * <include refid="Example_Where_Clause" />
     * </if>
     *
     * @param introspectedTable
     * @return
     */
    public static Element getExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));
        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);
        return ifElement;
    }

    /**
     * <if test="example != null">
     * <include refid="Update_By_Example_Where_Clause" />
     * </if>
     *
     * @param introspectedTable
     * @return
     */
    public static Element getUpdateByExampleIncludeElement(IntrospectedTable introspectedTable) {
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "example != null"));
        XmlElement includeElement = new XmlElement("include");
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);
        return ifElement;
    }

    public static Element getOrderByElement() {
        XmlElement orderByElement = new XmlElement("if");
        orderByElement.addAttribute(new Attribute("test", "_parameter != null and orderByClause != null"));
        orderByElement.addElement(new TextElement("order by ${orderByClause}"));
        return orderByElement;
    }

    public static Element getOrderByExampleElement() {
        XmlElement orderByElement = new XmlElement("if");
        orderByElement.addAttribute(new Attribute("test", "example != null and example.orderByClause != null"));
        orderByElement.addElement(new TextElement("order by ${example.orderByClause}"));
        return orderByElement;
    }

    public static Element getDistinctElement() {
        XmlElement ifDistinctElement = new XmlElement("if");
        ifDistinctElement.addAttribute(new Attribute("test", "_parameter != null and distinct"));
        ifDistinctElement.addElement(new TextElement("distinct"));
        return ifDistinctElement;
    }

    public static Element getDistinctByExampleElement() {
        XmlElement ifDistinctElement = new XmlElement("if");
        ifDistinctElement.addAttribute(new Attribute("test", "example != null and example.distinct"));
        ifDistinctElement.addElement(new TextElement("distinct"));
        return ifDistinctElement;
    }

    public static XmlElement getSelectiveElement(Element element) {
        XmlElement chooseElement = new XmlElement("choose");
        XmlElement whenElement = new XmlElement("when");
        whenElement.addAttribute(new Attribute("test", "columns != null and columns.length > 0"));
        chooseElement.addElement(whenElement);
        XmlElement foreachElement = new XmlElement("foreach");
        whenElement.addElement(foreachElement);
        foreachElement.addAttribute(new Attribute("collection", "columns"));
        foreachElement.addAttribute(new Attribute("item", "column"));
        foreachElement.addAttribute(new Attribute("separator", ","));
        foreachElement.addElement(element);
        return chooseElement;
    }

    public static Element generateKeys(List<IntrospectedColumn> columns, String prefix) {
        String keys = columns.stream().map(column -> {
            String name = MyBatis3FormattingUtilities.getEscapedColumnName(column);
            return prefix != null ? prefix + name : name;
        }).collect(Collectors.joining(", "));
        return new TextElement(keys);
    }

    public static TextElement generateValues(List<IntrospectedColumn> columns, String prefix) {
        String keys = columns.stream().map(column -> MyBatis3FormattingUtilities.getParameterClause(column, prefix)).collect(Collectors.joining(", "));
        return new TextElement("( " + keys + " )");
    }

    /**
     * 生成 xxxByPrimaryKey 的where 语句
     *
     * @param primaryKeyColumns
     * @param prefix
     * @return
     */
    public static TextElement generateWhereByPrimaryKeyTo(List<IntrospectedColumn> primaryKeyColumns, String prefix) {
        List<String> primaryKeys = new ArrayList<>();
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            primaryKeys.add(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn)
                + " = "
                + MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
        }
        return new TextElement(" where " + String.join(" and ", primaryKeys));
    }

    /**
     * @param valueStr
     * @param introspectedColumn
     * @return
     */
    public static String getParameterClauseWithColumn(String valueStr, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("#{");
        sb.append(valueStr);
        sb.append(",jdbcType=");
        sb.append(introspectedColumn.getJdbcTypeName());
        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(introspectedColumn.getTypeHandler());
        }
        sb.append('}');
        return sb.toString();
    }
}
