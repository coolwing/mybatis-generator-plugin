package com.wing.mybatis.comment;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.wing.mybatis.enhanced.InnerInterface;
import com.wing.mybatis.enhanced.InnerInterfaceWrapperToInnerClass;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import static com.wing.mybatis.comment.ParameterEnum.CLASS;
import static com.wing.mybatis.comment.ParameterEnum.COLUMN;
import static com.wing.mybatis.comment.ParameterEnum.DATE;
import static com.wing.mybatis.comment.ParameterEnum.ELEMENT;
import static com.wing.mybatis.comment.ParameterEnum.ENABLE_REMARK;
import static com.wing.mybatis.comment.ParameterEnum.ENUM;
import static com.wing.mybatis.comment.ParameterEnum.FIELD;
import static com.wing.mybatis.comment.ParameterEnum.FILE;
import static com.wing.mybatis.comment.ParameterEnum.INNER_CLASS;
import static com.wing.mybatis.comment.ParameterEnum.INNER_INTERFACE;
import static com.wing.mybatis.comment.ParameterEnum.METHOD;
import static com.wing.mybatis.comment.ParameterEnum.MGB;
import static com.wing.mybatis.comment.ParameterEnum.TABLE;

/**
 * 模版注释生成类
 *
 * @author wing
 * @date 2022/5/26
 **/
public class TemplateCommentGenerator extends DefaultCommentGenerator {
    private static final String TEMPLATE_PATH_NAME = "templatePath";
    //private static final String DEFAULT_TEMPLATE_PATH = "com/wing/mybatis/comment/simplest-comment.ftl";
    private static final String DEFAULT_TEMPLATE_NAME = "simplest-comment.ftl";

    private final Map<CommentNodeEnum, Template> templates = new HashMap<>();

    private boolean suppressAllComments = false;
    private boolean addRemarkComments = false;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.suppressAllComments = StringUtility.isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
        this.addRemarkComments = StringUtility.isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS));
        String templatePath = properties.getProperty(TEMPLATE_PATH_NAME);
        try {
            Document doc = null;
            if (StringUtility.stringHasValue(templatePath)) {
                File file = new File(templatePath);
                if (file.exists()) {
                    doc = new SAXReader().read(file);
                }
            } else {
                ClassLoader classLoader = this.getClass().getClassLoader();
                URL resource = classLoader.getResource(DEFAULT_TEMPLATE_NAME);
                if (resource != null) {
                    doc = new SAXReader().read(resource);
                }
            }
            if (doc != null) {
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
                for (CommentNodeEnum node : CommentNodeEnum.values()) {
                    Element element = doc.getRootElement().elementByID(node.value());
                    if (element != null) {
                        Template template = new Template(node.value(), element.getText(), cfg);
                        templates.put(node, template);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析注释结果
     *
     * @param map  模板参数
     * @param node 节点ID
     * @return
     */
    private String[] getComments(Map<String, Object> map, CommentNodeEnum node) {
        try {
            if (this.suppressAllComments) {
                return null;
            }
            StringWriter stringWriter = new StringWriter();
            Template template = templates.get(node);
            if (template != null) {
                template.process(map, stringWriter);
                String comment = stringWriter.toString();
                stringWriter.close();
                // 清理字符串
                String[] comments = comment.replaceFirst("^[\\s\\t\\r\\n]*", "").replaceFirst("[\\s\\t\\r\\n]*$", "").split("\n");
                // 去除空评论
                if (comments.length == 0 ||
                    comments.length == 1 && !StringUtility.stringHasValue(comments[0])) {
                    return null;
                }
                return comments;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 为xml元素添加注释
     *
     * @param xmlElement
     * @param map
     * @param node
     */
    private void addXmlElementComment(XmlElement xmlElement, Map<String, Object> map, CommentNodeEnum node) {
        String[] comments = getComments(map, node);
        if (comments == null) {
            return;
        }
        for (String comment : comments) {
            //在xml元素的开始位置添加注释，mgb覆盖时才可以识别
            xmlElement.addElement(0, new TextElement(comment));
        }
    }

    /**
     * 为文件添加注释
     *
     * @param compilationUnit
     * @param map
     * @param node
     */
    private void addCompilationUnitComment(CompilationUnit compilationUnit, Map<String, Object> map, CommentNodeEnum node) {
        String[] comments = getComments(map, node);
        if (comments == null) {
            return;
        }
        for (String comment : comments) {
            compilationUnit.addFileCommentLine(comment);
        }
    }

    /**
     * 为java元素添加注释
     *
     * @param javaElement
     * @param map
     * @param node
     */
    private void addJavaElementComment(JavaElement javaElement, Map<String, Object> map, CommentNodeEnum node) {
        String[] comments = getComments(map, node);
        if (comments == null) {
            return;
        }
        for (String comment : comments) {
            javaElement.addJavaDocLine(comment);
        }
    }

    private Map<String, Object> getBaseParamMap() {
        Map<String, Object> map = new HashMap<>(16);
        map.put(MGB.code(), MergeConstants.NEW_ELEMENT_TAG);
        map.put(ENABLE_REMARK.code(), this.addRemarkComments);
        map.put(DATE.code(), Optional.ofNullable(super.getDateString()).orElse(""));
        return map;
    }

    /**
     * 为xml文件添加注释（默认未添加）
     *
     * @param rootElement the root element
     */
    @Override
    public void addRootComment(XmlElement rootElement) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(ELEMENT.code(), rootElement);
        addXmlElementComment(rootElement, map, CommentNodeEnum.ADD_ROOT_COMMENT);
    }

    /**
     * 为xml文件中的元素增加注释
     * 只在文件覆盖时使用，不作具体注释
     *
     * @param xmlElement the xml element
     */
    @Override
    public void addComment(XmlElement xmlElement) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(ELEMENT.code(), xmlElement);
        addXmlElementComment(xmlElement, map, CommentNodeEnum.ADD_COMMENT);
    }

    /**
     * 为额外的java文件添加注释
     *
     * @param compilationUnit the compilation unit
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(FILE.code(), compilationUnit);
        addCompilationUnitComment(compilationUnit, map, CommentNodeEnum.ADD_JAVA_FILE_COMMENT);
    }

    /**
     * 为内部类添加注释
     *
     * @param innerClass        the inner class
     * @param introspectedTable the introspected table
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        Map<String, Object> map = this.getBaseParamMap();
        if (innerClass instanceof InnerInterfaceWrapperToInnerClass) {
            InnerInterface innerInterface = ((InnerInterfaceWrapperToInnerClass)innerClass).getInnerInterface();
            map.put(INNER_INTERFACE.code(), innerInterface);
            map.put(TABLE.code(), introspectedTable);
            addJavaElementComment(innerInterface, map, CommentNodeEnum.ADD_INTERFACE_COMMENT);
        } else {
            map.put(INNER_CLASS.code(), innerClass);
            map.put(TABLE.code(), introspectedTable);
            addJavaElementComment(innerClass, map, CommentNodeEnum.ADD_CLASS_COMMENT);
        }
    }

    /**
     * 为内部类添加注释
     *
     * @param innerClass        the inner class
     * @param introspectedTable the introspected table
     * @param markAsDoNotDelete the mark as do not delete
     */
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(INNER_CLASS.code(), innerClass);
        map.put(TABLE.code(), introspectedTable);
        map.put("markAsDoNotDelete", markAsDoNotDelete);
        addJavaElementComment(innerClass, map, CommentNodeEnum.ADD_CLASS_COMMENT);
    }

    /**
     * 为数据表的java模型类添加注释
     *
     * @param topLevelClass     the top level class
     * @param introspectedTable the introspected table
     */
    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(CLASS.code(), topLevelClass);
        map.put(TABLE.code(), introspectedTable);
        addJavaElementComment(topLevelClass, map, CommentNodeEnum.ADD_MODEL_CLASS_COMMENT);
    }

    /**
     * 为数据表字段添加注释
     *
     * @param field              the field
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(FIELD.code(), field);
        map.put(TABLE.code(), introspectedTable);
        map.put(COLUMN.code(), introspectedColumn);
        addJavaElementComment(field, map, CommentNodeEnum.ADD_FIELD_COMMENT);
    }

    /**
     * 为其他（除数据表对应模型）字段添加注释
     * 默认不添加
     *
     * @param field             the field
     * @param introspectedTable the introspected table
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(FIELD.code(), field);
        map.put(TABLE.code(), introspectedTable);
        addJavaElementComment(field, map, CommentNodeEnum.ADD_FIELD_COMMENT);
    }

    /**
     * 为get方法添加注释
     *
     * @param method             the method
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(METHOD.code(), method);
        map.put(TABLE.code(), introspectedTable);
        map.put(COLUMN.code(), introspectedColumn);
        addJavaElementComment(method, map, CommentNodeEnum.ADD_GETTER_COMMENT);
    }

    /**
     * 为set方法添加注释
     *
     * @param method             the method
     * @param introspectedTable  the introspected table
     * @param introspectedColumn the introspected column
     */
    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(METHOD.code(), method);
        map.put(TABLE.code(), introspectedTable);
        map.put(COLUMN.code(), introspectedColumn);
        addJavaElementComment(method, map, CommentNodeEnum.ADD_SETTER_COMMENT);
    }

    /**
     * 为java方法添加注释
     *
     * @param method            the method
     * @param introspectedTable the introspected table
     */
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(METHOD.code(), method);
        map.put(TABLE.code(), introspectedTable);
        addJavaElementComment(method, map, CommentNodeEnum.ADD_GENERAL_METHOD_COMMENT);
    }

    /**
     * 为枚举添加注释
     *
     * @param innerEnum         the inner enum
     * @param introspectedTable the introspected table
     */
    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        Map<String, Object> map = this.getBaseParamMap();
        map.put(ENUM.code(), innerEnum);
        map.put(TABLE.code(), introspectedTable);
        addJavaElementComment(innerEnum, map, CommentNodeEnum.ADD_ENUM_COMMENT);
    }

}
