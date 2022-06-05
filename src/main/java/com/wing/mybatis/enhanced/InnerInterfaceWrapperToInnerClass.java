package com.wing.mybatis.enhanced;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.InnerClass;

/**
 * 把InnerInterface包装成InnerClass(Mybatis Generator 没有提供内部接口实现)
 *
 * @author wing
 * @date 2022/5/26
 **/
public class InnerInterfaceWrapperToInnerClass extends InnerClass {
    private final InnerInterface innerInterface;

    public InnerInterfaceWrapperToInnerClass(InnerInterface innerInterface) {
        super(innerInterface.getType());
        this.innerInterface = innerInterface;
    }

    public InnerInterface getInnerInterface() {
        return innerInterface;
    }

    /**
     * 重写获取Java内容方法，调用InnerInterface的实现
     *
     * @param indentLevel
     * @param compilationUnit
     * @return
     */
    @Override
    public String getFormattedContent(int indentLevel, CompilationUnit compilationUnit) {
        return this.innerInterface.getFormattedContent(indentLevel, compilationUnit);
    }

}
