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

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * @desc 
 * @author zbss
 * @date 2017/11/17 22:17
 */
public class OneToOnePlugin extends PluginAdapter {

	/**
	 * 获取表配置
	 * @param tableName
	 * @param context
	 * @return
	 */
	private TableConfiguration getTableConfiguration(String tableName, Context context) {
		TableConfiguration tableConfiguration = null;
		for (TableConfiguration t : context.getTableConfigurations()) {
			if (t.getTableName().equalsIgnoreCase(tableName)) {
				tableConfiguration = t;
			}
		}
		return tableConfiguration;
	}

	/**
	 * 获取表
	 * @param tableName
	 * @param context
	 * @return
	 */
	private IntrospectedTable getIntrospectedTable(String tableName, Context context) {
		for (IntrospectedTable introspectedTable : context.getIntrospectedTables()) {
			introspectedTable.calculateJavaClientAttributes();
			introspectedTable.calculateXmlAttributes();
			if (introspectedTable.getTableConfiguration().getTableName().equalsIgnoreCase(tableName)) {
				return introspectedTable;
			}
		}
		return null;
	}

	/**
	 * 获取包名
	 * @param introspectedTable
	 * @param context
	 * @return
	 */
	private String getModelPackage(IntrospectedTable introspectedTable, Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getJavaModelGeneratorConfiguration().getTargetPackage());
		sb.append(introspectedTable.getFullyQualifiedTable().getSubPackageForModel(StringUtility.isTrue(context.getJavaModelGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))));
		String pkg = sb.toString();
		return pkg;
	}

	/**
	 * 修改model
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Context context = introspectedTable.getContext();
		for (OneToOne oto : introspectedTable.getOneToOnes()) {
			String tableName = oto.getMappingTable();
			TableConfiguration tc = getTableConfiguration(tableName, context);
			if (tc != null) {
				String pkg = getModelPackage(introspectedTable, context);
				String domainName = tc.getDomainObjectName();
				String type = pkg + "." + domainName;
				String fieldName = domainName.replaceFirst(new String(new char[]{domainName.charAt(0)}), new String(new char[]{domainName.charAt(0)}).toLowerCase());

				// 添加import语句
				topLevelClass.addImportedType(new FullyQualifiedJavaType(type));

				// 添加关联字段
				Field field = new Field();
				field.addJavaDocLine("/**" + MergeConstants.NEW_ELEMENT_TAG + " */");
				field.setName(fieldName);
				field.setType(new FullyQualifiedJavaType(type));
				field.setVisibility(JavaVisibility.PRIVATE);
				topLevelClass.addField(field);

				// 添加Get方法
				Method getMethod = new Method();
				getMethod.addJavaDocLine("/**" + MergeConstants.NEW_ELEMENT_TAG + " */");
				getMethod.setVisibility(JavaVisibility.PUBLIC);
				getMethod.setReturnType(new FullyQualifiedJavaType(type));
				getMethod.setName("get" + tc.getDomainObjectName());
				getMethod.addBodyLine("return " + fieldName + ";");
				topLevelClass.addMethod(getMethod);

				// 添加Set方法
				Method setMethod = new Method();
				setMethod.addJavaDocLine("/**" + MergeConstants.NEW_ELEMENT_TAG + " */");
				setMethod.setVisibility(JavaVisibility.PUBLIC);
				setMethod.setName("set" + tc.getDomainObjectName());
				setMethod.addParameter(new Parameter(new FullyQualifiedJavaType(type), fieldName));
				setMethod.addBodyLine("this." + fieldName + "=" + fieldName + ";");
				topLevelClass.addMethod(setMethod);
			}
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper.xml
	 * @param document
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		//获取
		Context context = introspectedTable.getContext();
		for (OneToOne oneToOne : introspectedTable.getOneToOnes()) {
			String tableName = oneToOne.getMappingTable();
			TableConfiguration tc = getTableConfiguration(tableName, context);
			IntrospectedTable it = getIntrospectedTable(tableName, context);
			if (tc != null) {
				String domainName = tc.getDomainObjectName();
				String fieldName = domainName.replaceFirst(new String(new char[]{domainName.charAt(0)}), new String(new char[]{domainName.charAt(0)}).toLowerCase());
				// 添加<association property="teacher" column="teacher_id" select="getTeacher"/>
				XmlElement assEle = new XmlElement("association");
				assEle.addAttribute(new Attribute("property", fieldName));
				assEle.addAttribute(new Attribute("column", oneToOne.getColumn()));
				assEle.addAttribute(new Attribute("select", "get" + domainName));
				for (Element ele : document.getRootElement().getElements()) {
					XmlElement xe = (XmlElement) ele;
					for (Attribute a : xe.getAttributes()) {
						if (a.getName().equalsIgnoreCase("id") && "BaseResultMap".equals(a.getValue())) {
							xe.addElement(assEle);
						}
					}
				}

				String columnName = oneToOne.getColumn();
				String columnJavaProp = "";
				List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
				for (IntrospectedColumn introspectedColumn : introspectedColumns){
					if (columnName.equals(introspectedColumn.getActualColumnName())){
						columnJavaProp = introspectedColumn.getJavaProperty();
					}
				}

				//添加查询方法<select id="testOutMapper" resultMap="soc.dao.ScanDao.BaseResultMap"><include refid="soc.dao.ScanDao.Base_Column_List" />
				XmlElement selectEle = new XmlElement("select");
				selectEle.addAttribute(new Attribute("id", "get" + domainName));
				selectEle.addAttribute(new Attribute("resultMap", it.getMyBatis3SqlMapNamespace() + ".BaseResultMap"));
				String sql = "SELECT ";
				sql += "<include refid=\""+it.getMyBatis3SqlMapNamespace()+".Base_Column_List"+"\" />";
				sql += " FROM " + tableName + " WHERE " + oneToOne.getJoinColumn() + "=#{" + columnJavaProp + "}";
				if (StringUtility.stringHasValue(oneToOne.getWhere())) {
					sql += " AND " + oneToOne.getWhere();
				}
				// 添加注释
				selectEle.addElement(new TextElement("<!--" + MergeConstants.NEW_ELEMENT_TAG + "-->"));
				selectEle.addElement(new TextElement(sql));
				document.getRootElement().addElement(selectEle);
			}
		}
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	/**
	 * 修改example
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	@Override
	public boolean validate(List<String> warnings) {
		// TODO Auto-generated method stub
		return true;
	}

}
