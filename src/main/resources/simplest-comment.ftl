<?xml version="1.0" encoding="UTF-8"?>
<template>

    <!-- 为java文件添加注释（未使用）-->
    <comment ID="addJavaFileComment"></comment>


    <!-- 为xml文件中的元素增加注释；只在文件覆盖时使用，不作具体注释 -->
    <comment ID="addComment">
        <![CDATA[
        <!-- WARNING - ${mgb} -->
        ]]>
    </comment>


    <!-- 为xml文件添加注释（默认不添加） -->
    <comment ID="addRootComment"></comment>


    <!-- 为java的字段添加注释。数据库字段会添加数据表中字段的注释，其他字段不会添加注释  -->
    <comment ID="addFieldComment">
        <![CDATA[
<#if column??>
    /**
 * ${table.fullyQualifiedTable}.${column.actualColumnName}
    <#if enableRemark && column.remarks?? && column.remarks != ''>
        <#list column.remarks?split("\n") as remark>
 * ${remark}
        </#list>
    </#if>
 *
 * ${mgb}
 */
</#if>
        ]]>
    </comment>


    <!-- 为数据表的java模型类添加注释 -->
    <comment ID="addModelClassComment">
        <![CDATA[
        /**
 * table name：${table.fullyQualifiedTable}
        <#if enableRemark && table.remarks?? && table.remarks != ''>
            <#list table.remarks?split("\n") as remark>
 * ${remark}
            </#list>
        </#if>
 *
 * ${mgb}
 * @date ${date}
 */
        ]]>
    </comment>


    <!-- 为内部接口添加注释（默认不添加）-->
    <comment ID="addInterfaceComment"></comment>


    <!-- 为内部类添加注释（默认不添加）-->
    <comment ID="addClassComment"></comment>


    <!-- 为枚举属性添加注释（默认不添加）-->
    <comment ID="addEnumComment"></comment>


    <!-- 为数据模型中get方法添加注释（默认不添加）-->
    <comment ID="addGetterComment"></comment>


    <!-- 为数据模型中set方法添加注释（默认不添加）-->
    <comment ID="addSetterComment"></comment>


    <!-- 为生成方法添加注释（默认不添加） -->
    <comment ID="addGeneralMethodComment"></comment>

</template>