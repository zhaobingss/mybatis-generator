package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TkMapperPlugin extends PluginAdapter {


    private final Map<String, TopLevelClass> topLevelClassMap = new HashMap<>();

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String name = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        topLevelClassMap.put(name, topLevelClass);
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        if (interfaze == null) {
            return true;
        }

        String name = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        TopLevelClass tpClass = topLevelClassMap.get(name);

        interfaze.addImportedType(new FullyQualifiedJavaType("tk.mybatis.mapper.common.Mapper"));
        interfaze.addImportedType(tpClass.getType());
        interfaze.addSuperInterface(new FullyQualifiedJavaType("Mapper<" + tpClass.getType().getShortName() + ">"));
        return true;

    }

}
