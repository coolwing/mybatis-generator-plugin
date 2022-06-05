package com.wing.mybatis.tools;

import java.util.List;

import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 格式化工具，优化输出
 *
 * @author wing
 * @date 2022/5/26
 **/
public class FormatTools {
    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String upFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 在类的最佳位置添加方法
     *
     * @param topLevelClass
     * @param method
     */
    public static void addMethodWithBestPosition(TopLevelClass topLevelClass, Method method) {
        addMethodWithBestPosition(method, topLevelClass.getMethods());
    }

    /**
     * 在接口的最佳位置添加方法
     *
     * @param interfacz
     * @param method
     */
    public static void addMethodWithBestPosition(Interface interfacz, Method method) {
        addMethodWithBestPosition(method, interfacz.getMethods());
    }

    /**
     * 在内部类最佳位置添加方法
     *
     * @param innerClass
     * @param method
     */
    public static void addMethodWithBestPosition(InnerClass innerClass, Method method) {
        addMethodWithBestPosition(method, innerClass.getMethods());
    }

    /**
     * 在枚举的最佳位置添加方法
     *
     * @param innerEnum
     * @param method
     */
    public static void addMethodWithBestPosition(InnerEnum innerEnum, Method method) {
        addMethodWithBestPosition(method, innerEnum.getMethods());
    }

    /**
     * 获取最佳添加位置
     *
     * @param method
     * @param methods
     * @return
     */
    private static void addMethodWithBestPosition(Method method, List<Method> methods) {
        int index = -1;
        for (int i = 0; i < methods.size(); i++) {
            Method m = methods.get(i);
            if (m.getName().equals(method.getName())) {
                if (m.getParameters().size() <= method.getParameters().size()) {
                    index = i + 1;
                } else {
                    index = i;
                }
            } else if (m.getName().startsWith(method.getName())) {
                if (index == -1) {
                    index = i;
                }
            } else if (method.getName().startsWith(m.getName())) {
                index = i + 1;
            }
        }
        if (index == -1 || index >= methods.size()) {
            methods.add(methods.size(), method);
        } else {
            methods.add(index, method);
        }
    }

    /**
     * 在最佳位置添加节点
     *
     * @param rootElement
     * @param element
     */
    public static void addElementWithBestPosition(XmlElement rootElement, XmlElement element) {
        // sql 元素都放在sql后面
        if (element.getName().equals("sql")) {
            int index = 0;
            for (Element ele : rootElement.getElements()) {
                if (ele instanceof XmlElement && ((XmlElement)ele).getName().equals("sql")) {
                    index++;
                }
            }
            rootElement.addElement(index, element);
        } else {
            // 根据id 排序
            String id = getIdFromElement(element);
            if (id == null) {
                rootElement.addElement(element);
            } else {
                List<Element> elements = rootElement.getElements();
                int index = -1;
                for (int i = 0; i < elements.size(); i++) {
                    Element ele = elements.get(i);
                    if (ele instanceof XmlElement) {
                        String eleId = getIdFromElement((XmlElement)ele);
                        if (eleId != null) {
                            if (eleId.startsWith(id)) {
                                if (index == -1) {
                                    index = i;
                                }
                            } else if (id.startsWith(eleId)) {
                                index = i + 1;
                            }
                        }
                    }
                }

                if (index == -1 || index >= elements.size()) {
                    rootElement.addElement(element);
                } else {
                    elements.add(index, element);
                }
            }
        }
    }

    /**
     * 找出节点ID值
     *
     * @param element
     * @return
     */
    private static String getIdFromElement(XmlElement element) {
        for (Attribute attribute : element.getAttributes()) {
            if (attribute.getName().equals("id")) {
                return attribute.getValue();
            }
        }
        return null;
    }

}
