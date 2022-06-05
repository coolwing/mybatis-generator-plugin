package com.wing.mybatis.plugins;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.ConnectionFactoryConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.TypedPropertyHolder;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * 执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
 *
 * @author wing
 * @date 2021/11/13
 **/
public abstract class BasePlugin extends PluginAdapter {
    private static final String MYBATIS_3_RUNNING_TIME = "MyBatis3";
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private static final String MYSQL_8_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    protected CommentGenerator commentGenerator;
    private static List<String> pluginNames = new ArrayList<>();

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        commentGenerator = context.getCommentGenerator();
        // mybatis版本
        List<PluginConfiguration> pluginConfigurations = (List<PluginConfiguration>)getProperty(context, "pluginConfigurations");
        if (pluginConfigurations != null) {
            pluginNames = pluginConfigurations.stream().map(TypedPropertyHolder::getConfigurationType).collect(Collectors.toList());
        }
    }

    private Object getProperty(final Object bean, final String name) {
        try {
            Field field = bean.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected boolean dependentPluginCheck(Class<?>... plugins) {
        for (Class<?> plugin : plugins) {
            if (!pluginNames.contains(plugin.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        try {
            final Context context = this.getContext();
            //只支持MyBatis3类型
            String targetRuntime = context.getTargetRuntime();
            if (!MYBATIS_3_RUNNING_TIME.equalsIgnoreCase(targetRuntime)) {
                warnings.add("targetRuntime只支持MyBatis3类型！");
                return false;
            }
            //只支持FLAT类型
            ModelType modelType = context.getDefaultModelType();
            if (ModelType.FLAT != modelType) {
                warnings.add("modelType只支持flat类型！");
                return false;
            }
            //驱动检查，只支持mysql
            JDBCConnectionConfiguration jdbcConnectionConfiguration = context.getJdbcConnectionConfiguration();
            if (jdbcConnectionConfiguration != null) {
                String driverClass = jdbcConnectionConfiguration.getDriverClass();
                System.out.println(jdbcConnectionConfiguration);
                if (!MYSQL_DRIVER_CLASS.equals(driverClass) && !MYSQL_8_DRIVER_CLASS.equals(driverClass)) {
                    warnings.add("只支持mysql数据库！");
                    return false;
                }
            }
            ConnectionFactoryConfiguration connectionFactoryConfiguration = context.getConnectionFactoryConfiguration();
            if (connectionFactoryConfiguration != null) {
                String driverClass = connectionFactoryConfiguration.getProperty("driverClass");
                System.out.println(connectionFactoryConfiguration);
                if (!MYSQL_DRIVER_CLASS.equals(driverClass) && !MYSQL_8_DRIVER_CLASS.equals(driverClass)) {
                    warnings.add("只支持mysql数据库！");
                    return false;
                }
            }

            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
            Properties javaModelProperties = javaModelGeneratorConfiguration.getProperties();
            if (!javaModelProperties.isEmpty()) {
                if (propertyTrueCheck(javaModelProperties, "immutable")) {
                    warnings.add("javaModelGenerator中不支持immutable属性！");
                    return false;
                }
            }
            JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = context.getJavaClientGeneratorConfiguration();
            if (!"XMLMAPPER".equals(javaClientGeneratorConfiguration.getConfigurationType())) {
                warnings.add("javaClientGenerator中只支持XMLMAPPER模式！");
                return false;
            }
            Properties javaClientProperties = javaClientGeneratorConfiguration.getProperties();
            if (!javaClientProperties.isEmpty()) {
                //if (javaClientProperties.getProperty("rootInterface") != null) {
                //    return false;
                //}
            }
            List<TableConfiguration> tableConfigurations = context.getTableConfigurations();
            for (TableConfiguration tableConfig : tableConfigurations) {
                //不支持表别名
                if (StringUtility.stringHasValue(tableConfig.getAlias())) {
                    return false;
                }
                if (propertyTrueCheck(tableConfig.getProperties(), "immutable")) {
                    warnings.add("javaModelGenerator中不支持immutable属性！");
                    return false;
                }
                //主键自增方式，只支持JDBC
                GeneratedKey generatedKey = tableConfig.getGeneratedKey();
                if (!"JDBC".equals(generatedKey.getRuntimeSqlStatement())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean propertyTrueCheck(Properties properties, String name) {
        return properties.getProperty(name) != null && Boolean.parseBoolean(properties.getProperty(name));
    }
}
