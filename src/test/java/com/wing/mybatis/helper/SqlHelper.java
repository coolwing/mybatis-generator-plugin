package com.wing.mybatis.helper;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

public class SqlHelper {
    /**
     * 通过Mapper接口和方法名(过滤掉回车多余空格信息，便于比对)
     *
     * @param mapper
     * @param methodName
     * @param args
     * @return
     */
    public static String getFormatMapperSql(Object mapper, String methodName, Object... args) {
        final String sql = getMapperSql(mapper, methodName, args);
        return sql == null ? null : sql.replaceAll("\n", " ").replaceAll("\\s+", " ");
    }

    /**
     * 通过接口获取sql
     *
     * @param mapper
     * @param methodName
     * @param args
     * @return
     */
    private static String getMapperSql(Object mapper, String methodName, Object... args) {
        final MetaObject metaObject = SystemMetaObject.forObject(mapper);
        final SqlSession session = (SqlSession)metaObject.getValue("h.sqlSession");
        final Class<?> mapperInterface = (Class<?>)metaObject.getValue("h.mapperInterface");
        final String fullMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMethodName, null);
        }
        final Method method = getDeclaredMethods(mapperInterface, methodName);
        if (args.length == 1 && !useParamAnnotation(method)) {
            return getNamespaceSql(session, fullMethodName, args[0]);
        }
        final Map<String, Object> param = getParamMap(method, args);
        return getNamespaceSql(session, fullMethodName, param);
    }

    /**
     * 通过命名空间方式获取sql
     *
     * @param session
     * @param namespace
     * @param params
     * @return
     */
    private static String getNamespaceSql(SqlSession session, String namespace, Object params) {
        final MappedStatement mappedStatement = session.getConfiguration().getMappedStatement(namespace);
        final BoundSql boundSql = mappedStatement.getBoundSql(params);
        try {
            final PreparedStatement preparedStatement = session.getConnection().prepareStatement(boundSql.getSql());
            final DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, params, boundSql);
            handler.setParameters(preparedStatement);
            return ((com.mysql.jdbc.PreparedStatement)preparedStatement).asSql();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String, Object> getParamMap(Method method, Object... args) {
        final Map<String, Object> params = new HashMap<>();
        final Class<?>[] argTypes = method.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (!RowBounds.class.isAssignableFrom(argTypes[i]) && !ResultHandler.class.isAssignableFrom(argTypes[i])) {
                String paramName = "param" + (params.size() + 1);
                final Object[] annotations = method.getParameterAnnotations()[i];
                for (Object annotation : annotations) {
                    if (annotation instanceof Param) {
                        paramName = ((Param)annotation).value();
                    }
                }
                params.put(paramName, i >= args.length ? null : args[i]);
            }
        }
        return params;
    }

    /**
     * 获取指定的方法
     *
     * @param clazz
     * @param methodName
     * @return
     */
    private static Method getDeclaredMethods(Class<?> clazz, String methodName) {
        final Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("方法" + methodName + "不存在！");
    }

    /**
     * 只有一个参数且没有使用注解
     *
     * @param method
     * @return
     */
    private static boolean useParamAnnotation(Method method) {
        final Object[] annotations = method.getParameterAnnotations()[0];
        for (Object annotation : annotations) {
            if (annotation instanceof Param) {
                return true;
            }
        }
        return false;
    }
}
