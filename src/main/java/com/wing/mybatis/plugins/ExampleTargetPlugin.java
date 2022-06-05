package com.wing.mybatis.plugins;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * Example类生成位置修改
 * （原配置项 javaModelGenerator exampleTargetPackage 不生效，使用插件代替）
 *
 * @author wing
 * @date 2021/11/19
 **/
public class ExampleTargetPlugin extends BasePlugin {
    public static final String PROPERTY_TARGET_PACKAGE = "targetPackage";
    private String targetPackage;

    @Override
    public boolean validate(List<String> warnings) {
        // 获取配置的目标package
        final Properties properties = getProperties();
        this.targetPackage = properties.getProperty(PROPERTY_TARGET_PACKAGE);
        if (!StringUtility.stringHasValue(this.targetPackage)) {
            return false;
        }
        return super.validate(warnings);
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        final JavaModelGeneratorConfiguration configuration = super.getContext().getJavaModelGeneratorConfiguration();
        //替换example文件的包路径
        final String newExampleType = introspectedTable.getExampleType().replace(configuration.getTargetPackage(), this.targetPackage);
        introspectedTable.setExampleType(newExampleType);
    }

}
