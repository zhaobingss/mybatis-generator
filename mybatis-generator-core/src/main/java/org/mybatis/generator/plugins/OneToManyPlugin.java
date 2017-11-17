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

public class OneToManyPlugin extends PluginAdapter {
	private TableConfiguration getMapTc(String tableName,Context context){
		TableConfiguration tc = null;
		for (TableConfiguration t : context.getTableConfigurations()) {
			if (t.getTableName().equalsIgnoreCase(tableName)) {
				tc = t;
			}
		}
		return tc;
	}
	private IntrospectedTable getIt(String tableName,Context context){
		
		for(IntrospectedTable i: context.getIntrospectedTables()){
			i.calculateJavaClientAttributes();
			i.calculateXmlAttributes();
			if(i.getTableConfiguration().getTableName().equalsIgnoreCase(tableName)){
				return i;
			}
		}
		return null;
	}
	private String getModelPackage( IntrospectedTable introspectedTable,Context context){
		StringBuilder sb = new StringBuilder();
		sb.append(context.getJavaModelGeneratorConfiguration().getTargetPackage());
		sb.append(introspectedTable.getFullyQualifiedTable().getSubPackageForModel(StringUtility.isTrue(context.getJavaModelGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))));
		String pakkage = sb.toString();
		return pakkage;
	}
	/**
	 * 修改model
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Context context=introspectedTable.getContext();
		for (OneToMany otm : introspectedTable.getOneToManys()) {
			String tableName = otm.getMappingTable();
			TableConfiguration tc =getMapTc(tableName, context);
			if (tc != null) {
				String pakkage = getModelPackage(introspectedTable, context);
				String domainName = tc.getDomainObjectName();
				String type=pakkage+"."+domainName;
				String fieldName = domainName.replaceFirst(new String(new char[] { domainName.charAt(0) }), new String(new char[] { domainName.charAt(0) }).toLowerCase())+"s";

				// import
				topLevelClass.addImportedType(new FullyQualifiedJavaType(type));
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));

				// field
				Field field = new Field();
				field.addJavaDocLine("/**"+ MergeConstants.NEW_ELEMENT_TAG + " */");
				field.setName(fieldName);
				FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.List<"+type+">");
				field.setType(fqjt);
				field.setVisibility(JavaVisibility.PRIVATE);
				topLevelClass.addField(field);

				// get
				Method getMethod = new Method();
				getMethod.addJavaDocLine("/**"+ MergeConstants.NEW_ELEMENT_TAG + " */");
				getMethod.setVisibility(JavaVisibility.PUBLIC);
				getMethod.setReturnType(fqjt);
				getMethod.setName("get" + tc.getDomainObjectName()+"s");
				getMethod.addBodyLine("return " + fieldName + ";");
				topLevelClass.addMethod(getMethod);
				// set
				Method setMethod = new Method();
				setMethod.addJavaDocLine("/**"+ MergeConstants.NEW_ELEMENT_TAG + " */");
				setMethod.setVisibility(JavaVisibility.PUBLIC);
				setMethod.setName("set" + tc.getDomainObjectName() + "s");
				setMethod.addParameter(new Parameter(fqjt, fieldName));
				setMethod.addBodyLine("this." + fieldName + "=" + fieldName + ";");
				topLevelClass.addMethod(setMethod);
			}
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改example
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper.xml
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		//获取
		Context context=introspectedTable.getContext();
		for (OneToMany otm : introspectedTable.getOneToManys()) {
			String tableName = otm.getMappingTable();
			TableConfiguration tc =getMapTc(tableName, context);
			IntrospectedTable it=getIt(tableName, context);
			if (tc != null) {
				String domainName = tc.getDomainObjectName()+"s";
				String fieldName = domainName.replaceFirst(new String(new char[] { domainName.charAt(0) }), new String(new char[] { domainName.charAt(0) }).toLowerCase());
				// 添加<collection property="tags" column="tag_id" select="getParentElement" />
				XmlElement assEle=new XmlElement("collection");
				assEle.addAttribute(new Attribute("property", fieldName));
				assEle.addAttribute(new Attribute("column", otm.getColumn()));
				assEle.addAttribute(new Attribute("select", "get"+domainName));
				for(Element ele:document.getRootElement().getElements()){
					XmlElement xe=(XmlElement)ele;
					for(Attribute a:xe.getAttributes()){
						if(a.getName().equalsIgnoreCase("id")&&"BaseResultMap".equals(a.getValue())){
							xe.addElement(assEle);
						}
					}
				}
				String tuofengColum="";
				boolean isUp=false;
				for(byte b:otm.getColumn().getBytes()){
					char c=(char)b;
					if(c=='_'){
						isUp=true;
					}else{
						if(isUp){
							tuofengColum+=new String(new char[]{c}).toUpperCase();
							isUp=false;
						}else{
							tuofengColum+=c;
						}
					}
				}
				//添加查询方法<select id="testOutMapper" resultMap="soc.dao.ScanDao.BaseResultMap"><include refid="soc.dao.ScanDao.Base_Column_List" />
				XmlElement selectEle=new XmlElement("select");
				selectEle.addAttribute(new Attribute("id", "get"+domainName));
				selectEle.addAttribute(new Attribute("resultMap", it.getMyBatis3SqlMapNamespace()+"."+"BaseResultMap"));
				String sql="SELECT ";
				/*for(IntrospectedColumn c:it.getAllColumns()){
					sql+=c.getActualColumnName()+",";
				}
				sql=sql.substring(0, sql.length()-1);*/
				sql += "<include refid=\""+it.getMyBatis3SqlMapNamespace()+".Base_Column_List"+"\" />";
				sql+=" FROM " +tableName+" WHERE "+otm.getJoinColumn()+"=#{"+tuofengColum+"} ";
				if(StringUtility.stringHasValue(otm.getWhere())){
					sql+=" AND "+otm.getWhere();
				}
				selectEle.addElement(new TextElement("<!--"+MergeConstants.NEW_ELEMENT_TAG+"-->"));
				selectEle.addElement(new TextElement(sql));
				document.getRootElement().addElement(selectEle);
			}
		}
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean validate(List<String> warnings) {
		// TODO Auto-generated method stub
		return true;
	}

}
