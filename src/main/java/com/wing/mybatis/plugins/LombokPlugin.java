package com.wing.mybatis.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * lombok插件，默认添加@Data注解，并删除getter、setter方法
 *
 * @author wing
 * @date 2021/11/19
 **/
public class LombokPlugin extends BasePlugin {

    private final static Set<String> SUPPORT_LOMBOK_ANNOTATIONS = new HashSet<String>() {{
        add("@Data");
        add("@NoArgsConstructor");
        add("@AllArgsConstructor");
        add("@Builder");
    }};
    /**
     * 存储需要添加的备注
     */
    private final List<String> annotations = new ArrayList<>();

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        final Set<String> leftAnnotations = new HashSet<>(SUPPORT_LOMBOK_ANNOTATIONS);
        for (Object key : properties.keySet()) {
            final String annotation = key.toString().trim();
            final String annotationName = annotation.split("\\(")[0];
            if (!leftAnnotations.contains(annotationName)) {
                //如果是不支持的注解，直接跳过
                continue;
            }
            //使用属性的value进行注解开关，enable=false可关闭注解
            final boolean enable = StringUtility.isTrue(properties.getProperty(key.toString()));
            //@Data不能关闭，会默认添加
            if (enable || annotation.startsWith("@Data")) {
                //使用从配置文件获取的注解，可支持注解的参数配置
                this.annotations.add(annotation);
            }
            leftAnnotations.remove(annotationName);
        }
        //默认添加未显式关闭的注解
        this.annotations.addAll(leftAnnotations);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (String annotation : this.annotations) {
            //如果配置了isConstructorBased属性，则不添加Constructor相关注解
            if (annotation.contains("Constructor") && introspectedTable.isConstructorBased()) {
                continue;
            }
            final String annotationName = annotation.split("\\(")[0].replace("@", "");
            topLevelClass.addImportedType("lombok." + annotationName);
            topLevelClass.addAnnotation(annotation);
        }
        return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //阻止生成getter方法
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //阻止生成setter方法
        return false;
    }
}
