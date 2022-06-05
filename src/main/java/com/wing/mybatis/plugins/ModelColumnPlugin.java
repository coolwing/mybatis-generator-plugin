package com.wing.mybatis.plugins;

import com.wing.mybatis.tools.FormatTools;
import com.wing.mybatis.tools.JavaElementGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;

/**
 * 在Example类中生成表字段对应的枚举
 *
 * @author wing
 * @date 2021/11/19
 **/
public class ModelColumnPlugin extends BasePlugin {

    public static final String ENUM_COLUMN = "Column";
    private static final String FIELD_COLUMN = "column";
    private static final String FIELD_JAVA_PROPERTY = "javaProperty";
    private static final String FIELD_JDBC_TYPE = "jdbcType";
    private static final String FIELD_IS_DELIMITED = "isDelimited";
    private static final String METHOD_GET_ESCAPED_COLUMN_NAME = "getDelimitedColumnName";
    private static final String METHOD_EXCLUDES = "excludes";
    private static final String METHOD_OF_COLUMN = "ofColumn";
    private String beginningDelimiter = "`";
    private String endingDelimiter = "`";

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.beginningDelimiter = context.getBeginningDelimiter();
        this.endingDelimiter = context.getEndingDelimiter();
        if ("\"".equals(this.beginningDelimiter)) {
            this.beginningDelimiter = "\\\"";
        }
        if ("\"".equals(this.endingDelimiter)) {
            this.endingDelimiter = "\\\"";
        }
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addInnerEnum(this.generateColumnEnum(topLevelClass, introspectedTable));
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    /**
     * 生成字段的枚举类
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private InnerEnum generateColumnEnum(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 生成内部枚举
        final InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(ENUM_COLUMN));
        innerEnum.setVisibility(JavaVisibility.PUBLIC);
        innerEnum.setStatic(true);
        commentGenerator.addEnumComment(innerEnum, introspectedTable);

        // 生成属性和构造函数
        final Field nameField = JavaElementGenerator.generatePrivateStringField(FIELD_COLUMN);
        nameField.setFinal(true);
        commentGenerator.addFieldComment(nameField, introspectedTable);
        innerEnum.addField(nameField);

        final Field javaPropertyField = JavaElementGenerator.generatePrivateStringField(FIELD_JAVA_PROPERTY);
        javaPropertyField.setFinal(true);
        commentGenerator.addFieldComment(javaPropertyField, introspectedTable);
        innerEnum.addField(javaPropertyField);

        final Field jdbcTypeField = JavaElementGenerator.generatePrivateStringField(FIELD_JDBC_TYPE);
        jdbcTypeField.setFinal(true);
        commentGenerator.addFieldComment(jdbcTypeField, introspectedTable);
        innerEnum.addField(jdbcTypeField);

        final Field isColumnNameDelimitedField = JavaElementGenerator.generateField(FIELD_IS_DELIMITED, JavaVisibility.PRIVATE, FullyQualifiedJavaType.getBooleanPrimitiveInstance(), null);
        isColumnNameDelimitedField.setFinal(true);
        commentGenerator.addFieldComment(isColumnNameDelimitedField, introspectedTable);
        innerEnum.addField(isColumnNameDelimitedField);

        final Method constructor = JavaElementGenerator.generateConstructorMethod(ENUM_COLUMN,
            new Parameter(FullyQualifiedJavaType.getStringInstance(), FIELD_COLUMN),
            new Parameter(FullyQualifiedJavaType.getStringInstance(), FIELD_JAVA_PROPERTY),
            new Parameter(FullyQualifiedJavaType.getStringInstance(), FIELD_JDBC_TYPE),
            new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), FIELD_IS_DELIMITED)
        );
        JavaElementGenerator.appendMethodBody(constructor,
            "this." + FIELD_COLUMN + " = " + FIELD_COLUMN + ";",
            "this." + FIELD_JAVA_PROPERTY + " = " + FIELD_JAVA_PROPERTY + ";",
            "this." + FIELD_JDBC_TYPE + " = " + FIELD_JDBC_TYPE + ";",
            "this." + FIELD_IS_DELIMITED + " = " + FIELD_IS_DELIMITED + ";")
        ;
        commentGenerator.addGeneralMethodComment(constructor, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, constructor);

        final Method getNameMethod = JavaElementGenerator.generateGetterMethod(nameField);
        commentGenerator.addGeneralMethodComment(getNameMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, getNameMethod);

        final Method getJavaPropertyMethod = JavaElementGenerator.generateGetterMethod(javaPropertyField);
        commentGenerator.addGeneralMethodComment(getJavaPropertyMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, getJavaPropertyMethod);

        final Method getJdbcTypeMethod = JavaElementGenerator.generateGetterMethod(jdbcTypeField);
        commentGenerator.addGeneralMethodComment(getJdbcTypeMethod, introspectedTable);
        FormatTools.addMethodWithBestPosition(innerEnum, getJdbcTypeMethod);

        // Enum枚举
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            String columnName = introspectedColumn.getJavaProperty().replaceAll("\\d+", "").replaceAll("(.)([A-Z])", "$1_$2").toUpperCase();
            String sb = columnName
                + "(\""
                + introspectedColumn.getActualColumnName()
                + "\", \""
                + introspectedColumn.getJavaProperty()
                + "\", \""
                + introspectedColumn.getJdbcTypeName()
                + "\", "
                + introspectedColumn.isColumnNameDelimited()
                + ")";
            innerEnum.addEnumConstant(sb);
        }

        // ofName
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Optional"));
        final Method ofNameMethod = JavaElementGenerator.generateMethod(
            METHOD_OF_COLUMN,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType(ENUM_COLUMN),
            new Parameter(FullyQualifiedJavaType.getStringInstance(), FIELD_COLUMN)
        );
        ofNameMethod.setStatic(true);
        commentGenerator.addGeneralMethodComment(ofNameMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            ofNameMethod,
            "Optional<" + ENUM_COLUMN + "> columnOptional = Arrays.stream(" + ENUM_COLUMN + ".values()).filter(unit -> unit." + FIELD_COLUMN + ".equals(" + FIELD_COLUMN + ")).findFirst();",
            "return columnOptional.orElse(null);"
        );
        FormatTools.addMethodWithBestPosition(innerEnum, ofNameMethod);

        // excludes
        topLevelClass.addImportedType("java.util.Arrays");
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        String paramName = "excludeColumns";
        final Method excludesMethod = JavaElementGenerator.generateMethod(
            METHOD_EXCLUDES,
            JavaVisibility.PUBLIC,
            new FullyQualifiedJavaType(ENUM_COLUMN + "[]"),
            new Parameter(innerEnum.getType(), paramName, true)
        );
        excludesMethod.setStatic(true);
        commentGenerator.addGeneralMethodComment(excludesMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            excludesMethod,
            "ArrayList<" + ENUM_COLUMN + "> columns = new ArrayList<>(Arrays.asList(values()));",
            "if (" + paramName + " != null && " + paramName + ".length > 0) {",
            "columns.removeAll(new ArrayList<>(Arrays.asList(" + paramName + ")));",
            "}",
            "return columns.toArray(new " + ENUM_COLUMN + "[]{});"
        );
        FormatTools.addMethodWithBestPosition(innerEnum, excludesMethod);

        // getDelimitedColumnName
        final Method getDelimitedColumnNameMethod = JavaElementGenerator.generateMethod(
            METHOD_GET_ESCAPED_COLUMN_NAME,
            JavaVisibility.PUBLIC,
            FullyQualifiedJavaType.getStringInstance()
        );
        commentGenerator.addGeneralMethodComment(getDelimitedColumnNameMethod, introspectedTable);
        JavaElementGenerator.appendMethodBody(
            getDelimitedColumnNameMethod,
            "if (this." + FIELD_IS_DELIMITED + ") {",
            "return \"" + this.beginningDelimiter + "\" + this." + FIELD_COLUMN + " + \"" + this.endingDelimiter + "\";",
            "} else {",
            "return this." + FIELD_COLUMN + ";",
            "}"
        );
        FormatTools.addMethodWithBestPosition(innerEnum, getDelimitedColumnNameMethod);

        return innerEnum;
    }
}
