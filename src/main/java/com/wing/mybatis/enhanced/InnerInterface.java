package com.wing.mybatis.enhanced;

import java.util.Iterator;
import java.util.Set;

import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.api.dom.java.Method;

import static org.mybatis.generator.api.dom.OutputUtilities.calculateImports;
import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.api.dom.OutputUtilities.newLine;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 内部接口
 *
 * @author wing
 * @date 2022/5/26
 **/
public class InnerInterface extends Interface {

    public InnerInterface(String type) {
        super(type);
    }

    /**
     * 格式化后内容，因为是内部接口，需要增加缩进
     *
     * @param indentLevel     the indent level
     * @param compilationUnit the compilation unit
     * @return the formatted content
     */
    @Override
    public String getFormattedContent(int indentLevel, CompilationUnit compilationUnit) {
        StringBuilder sb = new StringBuilder();

        for (String commentLine : getFileCommentLines()) {
            sb.append(commentLine);
            newLine(sb);
        }

        if (stringHasValue(getType().getPackageName())) {
            sb.append("package ");
            sb.append(getType().getPackageName());
            sb.append(';');
            newLine(sb);
            newLine(sb);
        }

        for (String staticImport : getStaticImports()) {
            sb.append("import static ");
            sb.append(staticImport);
            sb.append(';');
            newLine(sb);
        }

        if (getStaticImports().size() > 0) {
            newLine(sb);
        }

        Set<String> importStrings = calculateImports(getImportedTypes());
        for (String importString : importStrings) {
            sb.append(importString);
            newLine(sb);
        }

        if (importStrings.size() > 0) {
            newLine(sb);
        }

        addFormattedJavadoc(sb, indentLevel);
        addFormattedAnnotations(sb, indentLevel);

        OutputUtilities.javaIndent(sb, indentLevel);

        sb.append(getVisibility().getValue());

        if (isFinal()) {
            sb.append("final ");
        }

        sb.append("interface ");
        sb.append(getType().getShortName());

        if (getSuperInterfaceTypes().size() > 0) {
            sb.append(" extends ");

            boolean comma = false;
            for (FullyQualifiedJavaType fqjt : getSuperInterfaceTypes()) {
                if (comma) {
                    sb.append(", ");
                } else {
                    comma = true;
                }

                sb.append(JavaDomUtils.calculateTypeName(this, fqjt));
            }
        }

        sb.append(" {");
        indentLevel++;

        Iterator<Method> mtdIter = getMethods().iterator();
        while (mtdIter.hasNext()) {
            newLine(sb);
            Method method = mtdIter.next();
            sb.append(method.getFormattedContent(indentLevel, true, this));
            if (mtdIter.hasNext()) {
                newLine(sb);
            }
        }

        indentLevel--;
        newLine(sb);
        javaIndent(sb, indentLevel);
        sb.append('}');

        return sb.toString();
    }
}
