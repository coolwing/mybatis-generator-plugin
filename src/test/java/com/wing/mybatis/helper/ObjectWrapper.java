package com.wing.mybatis.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

public class ObjectWrapper {
    private final Class<?> clazz;
    private Object target;

    /**
     * 构造函数
     *
     * @param target
     */
    public ObjectWrapper(Object target) {
        this.target = target;
        this.clazz = target.getClass();
    }

    /**
     * 构造函数(枚举#分隔)
     *
     * @param loader
     * @param className
     */
    public ObjectWrapper(ClassLoader loader, String className) throws Exception {
        if (!className.contains("#")) {
            this.clazz = loader.loadClass(className);
            this.target = this.clazz.newInstance();
        } else {
            final String[] names = className.split("#");
            this.clazz = loader.loadClass(names[0]);
            if (this.clazz.isEnum()) {
                final Object[] constants = this.clazz.getEnumConstants();
                for (Object object : constants) {
                    ObjectWrapper enumObject = new ObjectWrapper(object);
                    if (names[1].equals(enumObject.invoke("name"))) {
                        this.target = object;
                        break;
                    }
                }
            } else {
                throw new ClassNotFoundException("没有找到对应枚举" + names[0]);
            }
        }
    }

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return clazz;
    }

    /**
     * 根据字段名，递归查询字段
     *
     * @param name
     * @return
     */
    private Field getDeclaredField(String name) {
        Class<?> clazz = this.clazz;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // 不能操作，递归父类
            }
        }
        return null;
    }

    /**
     * 获取指定名称的方法
     *
     * @param name
     * @return
     */
    public List<Method> getMethods(String name) {
        List<Method> list = new ArrayList<>();
        Class<?> clazz = this.clazz;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name)) {
                    list.add(method);
                }
            }
        }
        return list;
    }

    /**
     * 设置属性值
     * @param filedName
     * @param value
     * @return
     * @throws IllegalAccessException
     */
    public ObjectWrapper set(String filedName, Object value) throws IllegalAccessException {
        Field field = this.getDeclaredField(filedName);
        Assert.assertNotNull(field);
        field.setAccessible(true);
        field.set(this.target, value);
        return this;
    }

    /**
     * 获取属性值
     * @param filedName
     * @return
     * @throws IllegalAccessException
     */
    public Object get(String filedName) throws IllegalAccessException {
        Field field = this.getDeclaredField(filedName);
        Assert.assertNotNull(field);
        field.setAccessible(true);
        return field.get(this.target);
    }

    /**
     * 执行方法(mapper动态代理后VarArgs检查有问题)
     *
     * @param methodName
     * @param args
     * @return
     */
    public Object invoke(String methodName, Object... args) throws Exception {
        final List<Method> methods = getMethods(methodName);
        for (Method method : methods) {
            if (method.getParameters().length == 1 && args == null) {
                method.setAccessible(true);
                return method.invoke(this.target, new Object[] {null});
            } else if (method.getParameterTypes().length == args.length) {
                boolean flag = true;
                final Class<?>[] parameterTypes = method.getParameterTypes();
                // !! mapper动态代理后VarArgs检查有问题  暂时只检查前几位相同就假设为可变参数
                int check = parameterTypes.length > 0 ? parameterTypes.length - (parameterTypes[parameterTypes.length - 1].getName().startsWith("[") ? 1 : 0) : 0;
                for (int i = 0; i < check; i++) {
                    final Class<?> parameterType = parameterTypes[i];
                    if (args[i] != null && !(parameterType.isAssignableFrom(args[i].getClass()))) {
                        flag = false;
                    }
                    // 基础类型
                    if (parameterType.isPrimitive()) {
                        switch (parameterType.getTypeName()) {
                            case "boolean":
                                flag = args[i] instanceof Boolean;
                                break;
                            case "char":
                                flag = args[i] instanceof Character;
                                break;
                            case "byte":
                                flag = args[i] instanceof Byte;
                                break;
                            case "short":
                                flag = args[i] instanceof Short;
                                break;
                            case "int":
                                flag = args[i] instanceof Integer;
                                break;
                            case "long":
                                flag = args[i] instanceof Long;
                                break;
                            case "float":
                                flag = args[i] instanceof Float;
                                break;
                            case "double":
                                flag = args[i] instanceof Double;
                                break;
                            default:
                                flag = false;
                        }
                    }
                }
                if (flag) {
                    method.setAccessible(true);
                    return method.invoke(this.target, args);
                }
            }
        }
        throw new NoSuchMethodError("没有找到方法：" + methodName);
    }
}
