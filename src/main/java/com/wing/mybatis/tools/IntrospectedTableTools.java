package com.wing.mybatis.tools;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * IntrospectedTable 的一些拓展增强
 *
 * @author wing
 * @date 2022/5/26
 **/
public class IntrospectedTableTools {

    /**
     * 安全获取column 通过正则获取的name可能包含beginningDelimiter&&endingDelimiter
     *
     * @param introspectedTable
     * @param columnName
     * @return
     */
    public static IntrospectedColumn safeGetColumn(IntrospectedTable introspectedTable, String columnName) {
        String column = columnName.trim();
        String beginningDelimiter = introspectedTable.getContext().getBeginningDelimiter();
        if (StringUtility.stringHasValue(beginningDelimiter)) {
            column = column.replaceFirst("^" + beginningDelimiter, "");
        }
        String endingDelimiter = introspectedTable.getContext().getEndingDelimiter();
        if (StringUtility.stringHasValue(endingDelimiter)) {
            column = column.replaceFirst(endingDelimiter + "$", "");
        }
        return introspectedTable.getColumn(column);
    }

}
