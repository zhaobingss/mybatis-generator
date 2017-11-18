/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class BatchUpdatePlugin extends PluginAdapter{
	/**
	 * 修改Mapper类
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		addBatchUpdateMethod(interfaze, introspectedTable);
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}
	/**
	 * 修改Mapper.xml
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		addBatchUpdateXml(document, introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}
	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	private void addBatchUpdateMethod(Interface interfaze, IntrospectedTable introspectedTable) {
		// 设置需要导入的类
		Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
		importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
		importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

		Method ibsmethod = new Method();
		// 1.设置方法可见性
		ibsmethod.setVisibility(JavaVisibility.PUBLIC);
		// 2.设置返回值类型
		FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();
		ibsmethod.setReturnType(ibsreturnType);
		// 3.设置方法名
		ibsmethod.setName("updateBatchByPrimaryKeySelective");
		// 4.设置参数列表
		FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
		FullyQualifiedJavaType paramListType;
		if (introspectedTable.getRules().generateBaseRecordClass()) {
			paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		} else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
		} else {
			throw new RuntimeException(getString("RuntimeError.12"));
		}
		paramType.addTypeArgument(paramListType);

		ibsmethod.addParameter(new Parameter(paramType, "records"));

		interfaze.addImportedTypes(importedTypes);
		interfaze.addMethod(ibsmethod);
	}
	public void addBatchUpdateXml(Document document, IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
		String keyColumn=introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName();
		
		XmlElement insertBatchElement = new XmlElement("update");
		insertBatchElement.addAttribute(new Attribute("id", "updateBatchByPrimaryKeySelective"));
		insertBatchElement.addAttribute(new Attribute("parameterType", "java.util.List"));
		
		XmlElement foreach=new XmlElement("foreach");
		foreach.addAttribute(new Attribute("collection","list"));
		foreach.addAttribute(new Attribute("item","item"));
		foreach.addAttribute(new Attribute("index","index"));
		foreach.addAttribute(new Attribute("separator",";"));
		
		foreach.addElement(new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

		XmlElement trim1Element = new XmlElement("set");
		for (IntrospectedColumn introspectedColumn : columns) {
			String columnName = introspectedColumn.getActualColumnName();
			if(!columnName.toUpperCase().equalsIgnoreCase(keyColumn)){//不是自增字段的才会出现在批量插入中
				XmlElement ifxml=new XmlElement("if");
				ifxml.addAttribute(new Attribute("test", "item."+introspectedColumn.getJavaProperty()+"!=null"));
				ifxml.addElement(new TextElement(columnName+"=#{item."+introspectedColumn.getJavaProperty()+",jdbcType="+introspectedColumn.getJdbcTypeName() + "},"));
				trim1Element.addElement(ifxml);
			}
		}
		foreach.addElement(trim1Element);

		foreach.addElement(new TextElement("where "));
		int index=0;
		for(IntrospectedColumn i:introspectedTable.getPrimaryKeyColumns()){
			foreach.addElement(new TextElement((index>0?" AND ":"")+i.getActualColumnName()+" = #{item."+i.getJavaProperty()+",jdbcType="+i.getJdbcTypeName()+"}"));
		}
		
		insertBatchElement.addElement(foreach);

		document.getRootElement().addElement(insertBatchElement);
	}
}
