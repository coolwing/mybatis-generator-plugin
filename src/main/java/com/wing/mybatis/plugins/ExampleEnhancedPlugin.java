package com.wing.mybatis.plugins;

import com.wing.mybatis.enhanced.InnerInterface;
import com.wing.mybatis.enhanced.InnerInterfaceWrapperToInnerClass;
import com.wing.mybatis.tools.FormatTools;
import com.wing.mybatis.tools.JavaElementGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * Example增强插件
 *
 * @author wing
 * @date 2022/5/26
 **/
public class ExampleEnhancedPlugin extends BasePlugin {
    private static final String ENUM_SORT_TYPE = "SortType";
    private static final String METHOD_NEW_AND_CREATE_CRITERIA = "newAndCreateCriteria";
    private static final String METHOD_WHEN = "when";
    private static final String METHOD_ORDER_BY = "orderBy";
    private static final String METHOD_DISTINCT = "distinct";

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //1.为criteria添加对外部example的引用
        final InnerClass criteriaClass = topLevelClass.getInnerClasses().stream().filter(innerClass -> "Criteria".equals(innerClass.getType().getShortName())).findFirst().orElse(null);
        if (criteriaClass != null) {
            addFactoryMethodToCriteria(topLevelClass, criteriaClass, introspectedTable);
            //2.为criteria添加when方法
            addWhenToClass(topLevelClass, criteriaClass, introspectedTable, "criteria");
        }
        //增强criteria的创建方法，传入对外部example的引用
        final Method createMethod = topLevelClass.getMethods().stream().filter(method -> "createCriteriaInternal".equals(method.getName())).findFirst().orElse(null);
        if (createMethod != null) {
            createMethod.getBodyLines().set(0, "Criteria criteria = new Criteria(this);");
        }

        //3.newAndCreateCriteria 静态方法直接获取Criteria
        this.addStaticCreateCriteriaMethodToExample(topLevelClass, introspectedTable);

        //4.为example添加when方法
        this.addWhenToClass(topLevelClass, topLevelClass, introspectedTable, "example");

        //5.增强链式调用(distinct)
        this.addDistinctMethodToExample(topLevelClass, introspectedTable);

        //6.添加SortType枚举
        this.addSortTypeEnum(topLevelClass, introspectedTable);

        //6.添加orderBy方法
        this.addOrderByMethodToExample(topLevelClass, introspectedTable);
        if (dependentPluginCheck(ModelColumnPlugin.class)) {
            //添加基于ModelColumn的orderBy方法
            this.addOrderByWithColumnMethodToExample(topLevelClass, introspectedTable);
        }

        return true;
    }

    /**
     * 在Criteria中添加指向外部example的引用
     *
     * @param topLevelClass
     * @param innerClass
     * @param introspectedTable
     */
    private void addFactoryMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        final Field exampleField = JavaElementGenerator.generateField(
            "example",
            JavaVisibility.PRIVATE,
            topLevelClass.getType(),
            null
        );
        commentGenerator.addFieldComment(exampleField, introspectedTable);
        innerClass.addField(exampleField);

        //通过构造函数增加对外部example的引用；注意要修改创建Criteria的方法，增加传入参数
        final Method constructor = innerClass.getMethods().stream().filter(Method::isConstructor).findFirst().orElse(null);
        if (constructor != null) {
            constructor.addParameter(new Parameter(topLevelClass.getType(), "example"));
            constructor.addBodyLine("this.example = example;");
            commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        }

        //添加example工厂方法
        final Method exampleMethod = JavaElementGenerator.generateMethod(
            "example",
            JavaVisibility.PUBLIC,
            topLevelClass.getType()
        );
        commentGenerator.addGeneralMethodComment(exampleMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(exampleMethod,
            "return this.example;"
        );
        FormatTools.addMethodWithBestPosition(innerClass, exampleMethod);
    }

    /**
     * 添加 createCriteria 静态方法
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addStaticCreateCriteriaMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final Method createCriteriaMethod = JavaElementGenerator.generateMethod(
            METHOD_NEW_AND_CREATE_CRITERIA,
            JavaVisibility.PUBLIC,
            FullyQualifiedJavaType.getCriteriaInstance()
        );
        createCriteriaMethod.setStatic(true);
        commentGenerator.addGeneralMethodComment(createCriteriaMethod, introspectedTable);
        createCriteriaMethod.addBodyLine("return new " + topLevelClass.getType().getShortName() + "().createCriteria();");
        FormatTools.addMethodWithBestPosition(topLevelClass, createCriteriaMethod);
    }

    /**
     * 增强链式调用(when)
     *
     * @param topLevelClass
     * @param clazz
     * @param introspectedTable
     */
    private void addWhenToClass(TopLevelClass topLevelClass, InnerClass clazz, IntrospectedTable introspectedTable, String type) {
        // 添加接口When
        final InnerInterface whenInterface = new InnerInterface(FormatTools.upFirstChar(type) + FormatTools.upFirstChar(METHOD_WHEN));
        whenInterface.setVisibility(JavaVisibility.PUBLIC);

        // when接口中增加传递引用的方法
        final Method addMethod = JavaElementGenerator.generateMethod(
            type,
            JavaVisibility.DEFAULT,
            null,
            new Parameter(clazz.getType(), type)
        );
        commentGenerator.addGeneralMethodComment(addMethod, introspectedTable);
        whenInterface.addMethod(addMethod);

        final InnerClass innerClassWrapper = new InnerInterfaceWrapperToInnerClass(whenInterface);
        commentGenerator.addClassComment(innerClassWrapper, introspectedTable);
        topLevelClass.addInnerClass(innerClassWrapper);

        // 添加when方法
        final Method whenMethod = JavaElementGenerator.generateMethod(
            METHOD_WHEN,
            JavaVisibility.PUBLIC,
            clazz.getType(),
            new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "condition"),
            new Parameter(whenInterface.getType(), "then")
        );
        commentGenerator.addGeneralMethodComment(whenMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            whenMethod,
            "if (condition) {",
            "then." + type + "(this);",
            "}",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(clazz, whenMethod);

        //添加whenOtherwise方法
        final Method whenOtherwiseMethod = JavaElementGenerator.generateMethod(
            METHOD_WHEN,
            JavaVisibility.PUBLIC,
            clazz.getType(),
            new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "condition"),
            new Parameter(whenInterface.getType(), "then"),
            new Parameter(whenInterface.getType(), "otherwise")
        );
        commentGenerator.addGeneralMethodComment(whenOtherwiseMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            whenOtherwiseMethod,
            "if (condition) {",
            "then." + type + "(this);",
            "} else {",
            "otherwise." + type + "(this);",
            "}",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(clazz, whenOtherwiseMethod);
    }

    /**
     * 新增orderBy(String orderByClause)方法直接返回example
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addOrderByMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        final Method orderByMethod = JavaElementGenerator.generateMethod(
            METHOD_ORDER_BY,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(FullyQualifiedJavaType.getStringInstance(), "columnName"),
            new Parameter(new FullyQualifiedJavaType(ENUM_SORT_TYPE), "sortType")
        );
        commentGenerator.addGeneralMethodComment(orderByMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            orderByMethod,
            "if (orderByClause != null) {",
            "orderByClause = orderByClause + \", \" + columnName + \" \" + sortType.getValue();",
            "} else {",
            "orderByClause = columnName + \" \" + sortType.getValue();",
            "}",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, orderByMethod);
    }

    private void addOrderByWithColumnMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final Method orderByMethod = JavaElementGenerator.generateMethod(
            METHOD_ORDER_BY,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(new FullyQualifiedJavaType(ModelColumnPlugin.ENUM_COLUMN), "column"),
            new Parameter(new FullyQualifiedJavaType(ENUM_SORT_TYPE), "sortType")
        );
        commentGenerator.addGeneralMethodComment(orderByMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            orderByMethod,
            "return this.orderBy(column.getDelimitedColumnName(),sortType);"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, orderByMethod);
    }

    private void addSortTypeEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        final InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_SORT_TYPE));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);

        // 生成属性和构造函数
        final Field valueField = JavaElementGenerator.generatePrivateStringField("value");
        valueField.setFinal(true);
        commentGenerator.addFieldComment(valueField, introspectedTable);
        innerEnum.addField(valueField);

        final Method constructor = JavaElementGenerator.generateConstructorMethod(
            ENUM_SORT_TYPE,
            new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));
        JavaElementGenerator.appendMethodBody(
            constructor,
            "this.value = value;");
        commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, constructor);

        final Method getValueMethod = JavaElementGenerator.generateGetterMethod(valueField);
        commentGenerator.addGeneralMethodComment(getValueMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, getValueMethod);

        innerEnum.addEnumConstant("ASC(\"asc\")");
        innerEnum.addEnumConstant("DESC(\"desc\")");
        topLevelClass.addInnerEnum(innerEnum);
    }

    /**
     * 增强链式调用(distinct)
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addDistinctMethodToExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        final Method distinctMethod = JavaElementGenerator.generateMethod(
            METHOD_DISTINCT,
            JavaVisibility.PUBLIC,
            topLevelClass.getType(),
            new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "distinct")
        );
        commentGenerator.addGeneralMethodComment(distinctMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            distinctMethod,
            "this.setDistinct(distinct);",
            "return this;"
        );
        FormatTools.addMethodWithBestPosition(topLevelClass, distinctMethod);
    }
}
