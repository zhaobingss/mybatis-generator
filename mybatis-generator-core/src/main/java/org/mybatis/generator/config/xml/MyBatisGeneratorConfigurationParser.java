/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.config.xml;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.ObjectFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class parses configuration files into the new Configuration API.
 * @author Jeff Butler
 */
public class MyBatisGeneratorConfigurationParser {
	private Properties extraProperties;
	private Properties configurationProperties;

	public MyBatisGeneratorConfigurationParser(Properties extraProperties) {
		super();
		if (extraProperties == null) {
			this.extraProperties = new Properties();
		} else {
			this.extraProperties = extraProperties;
		}
		configurationProperties = new Properties();
	}

	public Configuration parseConfiguration(Element rootNode)
			throws XMLParserException {

		Configuration configuration = new Configuration();

		NodeList nodeList = rootNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("properties".equals(childNode.getNodeName())) {
				parseProperties(configuration, childNode);
			} else if ("property".equals(childNode.getNodeName())) {
				parseGlobalProperty(childNode);
			} else if ("classPathEntry".equals(childNode.getNodeName())) {
				parseClassPathEntry(configuration, childNode);
			} else if ("context".equals(childNode.getNodeName())) {
				parseContext(configuration, childNode);
			}
		}

		return configuration;
	}

	protected void parseProperties(Configuration configuration, Node node)
			throws XMLParserException {
		Properties attributes = parseAttributes(node);
		String resource = attributes.getProperty("resource");
		String url = attributes.getProperty("url");

		if (!stringHasValue(resource) && !stringHasValue(url)) {
			throw new XMLParserException(getString("RuntimeError.14"));
		}

		if (stringHasValue(resource) && stringHasValue(url)) {
			throw new XMLParserException(getString("RuntimeError.14"));
		}

		URL resourceUrl;
		try {
			if (stringHasValue(resource)) {
				resourceUrl = ObjectFactory.getResource(resource);
				if (resourceUrl == null) {
					throw new XMLParserException(getString("RuntimeError.15", resource));
				}
			} else {
				resourceUrl = new URL(url);
			}
			InputStream inputStream = resourceUrl.openConnection().getInputStream();
			configurationProperties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			if (stringHasValue(resource)) {
				throw new XMLParserException(getString("RuntimeError.16", resource));
			} else {
				throw new XMLParserException(getString("RuntimeError.17", url));
			}
		}
	}

	private void parseContext(Configuration configuration, Node node) throws XMLParserException {
		Properties attributes = parseAttributes(node);
		String defaultModelType = attributes.getProperty("defaultModelType");
		String targetRuntime = attributes.getProperty("targetRuntime");
		String introspectedColumnImpl = attributes.getProperty("introspectedColumnImpl");
		String id = attributes.getProperty("id");

		ModelType mt = defaultModelType == null ? null : ModelType.getModelType(defaultModelType);
		Context context = new Context(mt);
		context.setId(id);
		if (stringHasValue(introspectedColumnImpl)) {
			context.setIntrospectedColumnImpl(introspectedColumnImpl);
		}
		if (stringHasValue(targetRuntime)) {
			context.setTargetRuntime(targetRuntime);
		}

		configuration.addContext(context);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(context, childNode);
			} else if ("plugin".equals(childNode.getNodeName())) {
				parsePlugin(context, childNode);
			} else if ("commentGenerator".equals(childNode.getNodeName())) {
				parseCommentGenerator(context, childNode);
			} else if ("jdbcConnection".equals(childNode.getNodeName())) {
				parseJdbcConnection(context, childNode);
			} else if ("connectionFactory".equals(childNode.getNodeName())) {
				parseConnectionFactory(context, childNode);
			} else if ("javaModelGenerator".equals(childNode.getNodeName())) {
				parseJavaModelGenerator(context, childNode);
			} else if ("javaTypeResolver".equals(childNode.getNodeName())) {
				parseJavaTypeResolver(context, childNode);
			} else if ("sqlMapGenerator".equals(childNode.getNodeName())) {
				parseSqlMapGenerator(context, childNode);
			} else if ("javaClientGenerator".equals(childNode.getNodeName())) {
				parseJavaClientGenerator(context, childNode);
			} else if ("table".equals(childNode.getNodeName())) {
				parseTable(context, childNode);
			}
		}
	}

	protected void parseSqlMapGenerator(Context context, Node node) {
		SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
		context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String targetPackage = attributes.getProperty("targetPackage");
		String targetProject = attributes.getProperty("targetProject");

		sqlMapGeneratorConfiguration.setTargetPackage(targetPackage);
		sqlMapGeneratorConfiguration.setTargetProject(targetProject);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("property".equals(childNode.getNodeName())) {
				parseProperty(sqlMapGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseTable(Context context, Node node) throws XMLParserException {
		TableConfiguration tc = new TableConfiguration(context);
		context.addTableConfiguration(tc);

		Properties attributes = parseAttributes(node);

		String catalog = attributes.getProperty("catalog");
		if (stringHasValue(catalog)) {
			tc.setCatalog(catalog);
		}

		String schema = attributes.getProperty("schema");
		if (stringHasValue(schema)) {
			tc.setSchema(schema);
		}

		String tableName = attributes.getProperty("tableName");
		if (stringHasValue(tableName)) {
			tc.setTableName(tableName);
		}

		String domainObjectName = attributes.getProperty("domainObjectName");
		if (stringHasValue(domainObjectName)) {
			tc.setDomainObjectName(domainObjectName);
		}

		String alias = attributes.getProperty("alias");
		if (stringHasValue(alias)) {
			tc.setAlias(alias);
		}

		String enableInsert = attributes.getProperty("enableInsert");
		if (stringHasValue(enableInsert)) {
			tc.setInsertStatementEnabled(isTrue(enableInsert));
		}

		String enableSelectByPrimaryKey = attributes.getProperty("enableSelectByPrimaryKey");
		if (stringHasValue(enableSelectByPrimaryKey)) {
			tc.setSelectByPrimaryKeyStatementEnabled(isTrue(enableSelectByPrimaryKey));
		}

		String enableSelectByExample = attributes.getProperty("enableSelectByExample");
		if (stringHasValue(enableSelectByExample)) {
			tc.setSelectByExampleStatementEnabled(isTrue(enableSelectByExample));
		}

		String enableUpdateByPrimaryKey = attributes.getProperty("enableUpdateByPrimaryKey");
		if (stringHasValue(enableUpdateByPrimaryKey)) {
			tc.setUpdateByPrimaryKeyStatementEnabled(isTrue(enableUpdateByPrimaryKey));
		}

		String enableDeleteByPrimaryKey = attributes.getProperty("enableDeleteByPrimaryKey");
		if (stringHasValue(enableDeleteByPrimaryKey)) {
			tc.setDeleteByPrimaryKeyStatementEnabled(isTrue(enableDeleteByPrimaryKey));
		}

		String enableDeleteByExample = attributes.getProperty("enableDeleteByExample");
		if (stringHasValue(enableDeleteByExample)) {
			tc.setDeleteByExampleStatementEnabled(isTrue(enableDeleteByExample));
		}

		String enableCountByExample = attributes.getProperty("enableCountByExample");
		if (stringHasValue(enableCountByExample)) {
			tc.setCountByExampleStatementEnabled(isTrue(enableCountByExample));
		}

		String enableUpdateByExample = attributes.getProperty("enableUpdateByExample");
		if (stringHasValue(enableUpdateByExample)) {
			tc.setUpdateByExampleStatementEnabled(isTrue(enableUpdateByExample));
		}

		String selectByPrimaryKeyQueryId = attributes.getProperty("selectByPrimaryKeyQueryId");
		if (stringHasValue(selectByPrimaryKeyQueryId)) {
			tc.setSelectByPrimaryKeyQueryId(selectByPrimaryKeyQueryId);
		}

		String selectByExampleQueryId = attributes.getProperty("selectByExampleQueryId");
		if (stringHasValue(selectByExampleQueryId)) {
			tc.setSelectByExampleQueryId(selectByExampleQueryId);
		}

		String modelType = attributes.getProperty("modelType");
		if (stringHasValue(modelType)) {
			tc.setConfiguredModelType(modelType);
		}

		String escapeWildcards = attributes.getProperty("escapeWildcards");
		if (stringHasValue(escapeWildcards)) {
			tc.setWildcardEscapingEnabled(isTrue(escapeWildcards));
		}

		String delimitIdentifiers = attributes.getProperty("delimitIdentifiers");
		if (stringHasValue(delimitIdentifiers)) {
			tc.setDelimitIdentifiers(isTrue(delimitIdentifiers));
		}

		String delimitAllColumns = attributes.getProperty("delimitAllColumns");
		if (stringHasValue(delimitAllColumns)) {
			tc.setAllColumnDelimitingEnabled(isTrue(delimitAllColumns));
		}

		String mapperName = attributes.getProperty("mapperName");
		if (stringHasValue(mapperName)) {
			tc.setMapperName(mapperName);
		}

		String sqlProviderName = attributes.getProperty("sqlProviderName");
		if (stringHasValue(sqlProviderName)) {
			tc.setSqlProviderName(sqlProviderName);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("property".equals(childNode.getNodeName())) {
				parseProperty(tc, childNode);
			} else if ("columnOverride".equals(childNode.getNodeName())) {
				parseColumnOverride(tc, childNode);
			} else if ("ignoreColumn".equals(childNode.getNodeName())) {
				parseIgnoreColumn(tc, childNode);
			} else if ("ignoreColumnsByRegex".equals(childNode.getNodeName())) {
				parseIgnoreColumnByRegex(tc, childNode);
			} else if ("generatedKey".equals(childNode.getNodeName())) {
				parseGeneratedKey(tc, childNode);
			} else if ("domainObjectRenamingRule".equals(childNode.getNodeName())) {
				parseDomainObjectRenamingRule(tc, childNode);
			} else if ("columnRenamingRule".equals(childNode.getNodeName())) {
				parseColumnRenamingRule(tc, childNode);
			} else if ("oneToOne".equals(childNode.getNodeName())) {
				//一对一配置
				parseOneToOne(tc, childNode);
			} else if ("oneToMany".equals(childNode.getNodeName())) {
				//一对多配置
				parseOneToMany(tc, childNode);
			} else if ("manyToMany".equals(childNode.getNodeName())) {
				//多对多配置
				parseManyToMany(tc, childNode);
			}
		}
	}

	private void parseOneToOne(TableConfiguration tc, Node node) throws XMLParserException {
		Properties attributes = parseAttributes(node);
		String mappingTable = attributes.getProperty("mappingTable");
		String column = attributes.getProperty("column");
		if (!stringHasValue(mappingTable)) {
			throw new XMLParserException(getString("RuntimeError.23", mappingTable));
		} else if (!stringHasValue(column)) {
			throw new XMLParserException(getString("RuntimeError.24", column));
		}
		OneToOne oto = new OneToOne(mappingTable, column);
		String joinColumn = attributes.getProperty("joinColumn");
		if (stringHasValue(joinColumn)) {
			oto.setJoinColumn(joinColumn);
		}
		String where = attributes.getProperty("where");
		if (stringHasValue(where)) {
			oto.setWhere(where);
		}
		tc.getOneToOnes().add(oto);
	}

	private void parseOneToMany(TableConfiguration tc, Node node) throws XMLParserException {
		Properties attributes = parseAttributes(node);
		String mappingTable = attributes.getProperty("mappingTable");
		String column = attributes.getProperty("column");
		if (!stringHasValue(mappingTable)) {
			throw new XMLParserException(getString("RuntimeError.23", mappingTable));
		} else if (!stringHasValue(column)) {
			throw new XMLParserException(getString("RuntimeError.24", column));
		}
		OneToMany oto = new OneToMany(mappingTable, column);
		String joinColumn = attributes.getProperty("joinColumn");
		if (stringHasValue(joinColumn)) {
			oto.setJoinColumn(joinColumn);
		}
		String where = attributes.getProperty("where");
		if (stringHasValue(where)) {
			oto.setWhere(where);
		}
		tc.getOneToManys().add(oto);
	}

	private void parseManyToMany(TableConfiguration tc, Node node) throws XMLParserException {
		Properties attributes = parseAttributes(node);

		String column = attributes.getProperty("column");
		if (!stringHasValue(column)) {
			throw new XMLParserException("主表关联字段不能为空！");
		}

		String mappingTable = attributes.getProperty("mappingTable");
		if (!stringHasValue(mappingTable)) {
			throw new XMLParserException("对应表不能为空！");
		}

		String joinColumn = attributes.getProperty("joinColumn");
		if (!stringHasValue(joinColumn)) {
			throw new XMLParserException("对应表的关联字段不能为空！");
		}

		String relateTable = attributes.getProperty("relateTable");
		if (!stringHasValue(relateTable)){
			throw new XMLParserException("关联表不能为空！");
		}

		String relateColumn = attributes.getProperty("relateColumn");
		if (!stringHasValue(relateColumn)){
			throw new XMLParserException("主表在关联表中的字段不能为空！");
		}

		String relateJoinColumn = attributes.getProperty("relateJoinColumn");
		if (!stringHasValue(relateJoinColumn)){
			throw new XMLParserException("对应表在关联表中的字段不能为空！");
		}

		ManyToMany manyToMany = new ManyToMany();
		manyToMany.setColumn(column);
		manyToMany.setMappingTable(mappingTable);
		manyToMany.setJoinColumn(joinColumn);
		manyToMany.setRelateTable(relateTable);
		manyToMany.setRelateColumn(relateColumn);
		manyToMany.setRelateJoinColumn(relateJoinColumn);

		String where = attributes.getProperty("where");
		if (stringHasValue(where)) {
			manyToMany.setWhere(where);
		}

		tc.getManyToManys().add(manyToMany);
	}

	private void parseColumnOverride(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column");

		ColumnOverride co = new ColumnOverride(column);

		String property = attributes.getProperty("property");
		if (stringHasValue(property)) {
			co.setJavaProperty(property);
		}

		String javaType = attributes.getProperty("javaType");
		if (stringHasValue(javaType)) {
			co.setJavaType(javaType);
		}

		String jdbcType = attributes.getProperty("jdbcType");
		if (stringHasValue(jdbcType)) {
			co.setJdbcType(jdbcType);
		}

		String typeHandler = attributes.getProperty("typeHandler");
		if (stringHasValue(typeHandler)) {
			co.setTypeHandler(typeHandler);
		}

		String delimitedColumnName = attributes.getProperty("delimitedColumnName");
		if (stringHasValue(delimitedColumnName)) {
			co.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		String isGeneratedAlways = attributes.getProperty("isGeneratedAlways");
		if (stringHasValue(isGeneratedAlways)) {
			co.setGeneratedAlways(Boolean.parseBoolean(isGeneratedAlways));
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("property".equals(childNode.getNodeName())) {
				parseProperty(co, childNode);
			}
		}

		tc.addColumnOverride(co);
	}

	private void parseGeneratedKey(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);

		String column = attributes.getProperty("column");
		boolean identity = isTrue(attributes.getProperty("identity"));
		String sqlStatement = attributes.getProperty("sqlStatement");
		String type = attributes.getProperty("type");

		GeneratedKey gk = new GeneratedKey(column, sqlStatement, identity, type);
		tc.setGeneratedKey(gk);
	}

	private void parseIgnoreColumn(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column");
		String delimitedColumnName = attributes
				.getProperty("delimitedColumnName");

		IgnoredColumn ic = new IgnoredColumn(column);

		if (stringHasValue(delimitedColumnName)) {
			ic.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		tc.addIgnoredColumn(ic);
	}

	private void parseIgnoreColumnByRegex(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String pattern = attributes.getProperty("pattern");

		IgnoredColumnPattern icPattern = new IgnoredColumnPattern(pattern);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("except".equals(childNode.getNodeName())) {
				parseException(icPattern, childNode);
			}
		}

		tc.addIgnoredColumnPattern(icPattern);
	}

	private void parseException(IgnoredColumnPattern icPattern, Node node) {
		Properties attributes = parseAttributes(node);
		String column = attributes.getProperty("column");
		String delimitedColumnName = attributes.getProperty("delimitedColumnName");

		IgnoredColumnException exception = new IgnoredColumnException(column);

		if (stringHasValue(delimitedColumnName)) {
			exception.setColumnNameDelimited(isTrue(delimitedColumnName));
		}

		icPattern.addException(exception);
	}

	private void parseDomainObjectRenamingRule(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String searchString = attributes.getProperty("searchString");
		String replaceString = attributes.getProperty("replaceString");

		DomainObjectRenamingRule dorr = new DomainObjectRenamingRule();

		dorr.setSearchString(searchString);

		if (stringHasValue(replaceString)) {
			dorr.setReplaceString(replaceString);
		}

		tc.setDomainObjectRenamingRule(dorr);
	}

	private void parseColumnRenamingRule(TableConfiguration tc, Node node) {
		Properties attributes = parseAttributes(node);
		String searchString = attributes.getProperty("searchString");
		String replaceString = attributes.getProperty("replaceString");

		ColumnRenamingRule crr = new ColumnRenamingRule();

		crr.setSearchString(searchString);

		if (stringHasValue(replaceString)) {
			crr.setReplaceString(replaceString);
		}

		tc.setColumnRenamingRule(crr);
	}

	protected void parseJavaTypeResolver(Context context, Node node) {
		JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();

		context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type");

		if (stringHasValue(type)) {
			javaTypeResolverConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(javaTypeResolverConfiguration, childNode);
			}
		}
	}

	private void parsePlugin(Context context, Node node) {
		PluginConfiguration pluginConfiguration = new PluginConfiguration();

		context.addPluginConfiguration(pluginConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type");

		pluginConfiguration.setConfigurationType(type);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(pluginConfiguration, childNode);
			}
		}
	}

	protected void parseJavaModelGenerator(Context context, Node node) {
		JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();

		context
				.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String targetPackage = attributes.getProperty("targetPackage");
		String targetProject = attributes.getProperty("targetProject");

		javaModelGeneratorConfiguration.setTargetPackage(targetPackage);
		javaModelGeneratorConfiguration.setTargetProject(targetProject);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(javaModelGeneratorConfiguration, childNode);
			}
		}
	}

	private void parseJavaClientGenerator(Context context, Node node) {
		JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();

		context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type");
		String targetPackage = attributes.getProperty("targetPackage");
		String targetProject = attributes.getProperty("targetProject");
		String implementationPackage = attributes
				.getProperty("implementationPackage");

		javaClientGeneratorConfiguration.setConfigurationType(type);
		javaClientGeneratorConfiguration.setTargetPackage(targetPackage);
		javaClientGeneratorConfiguration.setTargetProject(targetProject);
		javaClientGeneratorConfiguration
				.setImplementationPackage(implementationPackage);

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(javaClientGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseJdbcConnection(Context context, Node node) {
		JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();

		context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

		Properties attributes = parseAttributes(node);
		String driverClass = attributes.getProperty("driverClass");
		String connectionURL = attributes.getProperty("connectionURL");

		jdbcConnectionConfiguration.setDriverClass(driverClass);
		jdbcConnectionConfiguration.setConnectionURL(connectionURL);

		String userId = attributes.getProperty("userId");
		if (stringHasValue(userId)) {
			jdbcConnectionConfiguration.setUserId(userId);
		}

		String password = attributes.getProperty("password");
		if (stringHasValue(password)) {
			jdbcConnectionConfiguration.setPassword(password);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(jdbcConnectionConfiguration, childNode);
			}
		}
	}

	protected void parseClassPathEntry(Configuration configuration, Node node) {
		Properties attributes = parseAttributes(node);
		configuration.addClasspathEntry(attributes.getProperty("location"));
	}

	protected void parseProperty(PropertyHolder propertyHolder, Node node) {
		Properties attributes = parseAttributes(node);

		String name = attributes.getProperty("name");
		String value = attributes.getProperty("value");

		propertyHolder.addProperty(name, value);
	}

	protected void parseGlobalProperty(Node node) {
		Properties attributes = parseAttributes(node);
		String name = attributes.getProperty("name");
		String value = attributes.getProperty("value");
		Configuration.properties.setProperty(name, value);
	}

	protected Properties parseAttributes(Node node) {
		Properties attributes = new Properties();
		NamedNodeMap nnm = node.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Node attribute = nnm.item(i);
			String value = parsePropertyTokens(attribute.getNodeValue());
			attributes.put(attribute.getNodeName(), value);
		}

		return attributes;
	}

	private String parsePropertyTokens(String string) {
		final String OPEN = "${";
		final String CLOSE = "}";

		String newString = string;
		if (newString != null) {
			int start = newString.indexOf(OPEN);
			int end = newString.indexOf(CLOSE);

			while (start > -1 && end > start) {
				String prepend = newString.substring(0, start);
				String append = newString.substring(end + CLOSE.length());
				String propName = newString.substring(start + OPEN.length(),
						end);
				String propValue = resolveProperty(propName);
				if (propValue != null) {
					newString = prepend + propValue + append;
				}

				start = newString.indexOf(OPEN, end);
				end = newString.indexOf(CLOSE, end);
			}
		}

		return newString;
	}

	protected void parseCommentGenerator(Context context, Node node) {
		CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
		context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type");

		if (stringHasValue(type)) {
			commentGeneratorConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if ("property".equals(childNode.getNodeName())) {
				parseProperty(commentGeneratorConfiguration, childNode);
			}
		}
	}

	protected void parseConnectionFactory(Context context, Node node) {
		ConnectionFactoryConfiguration connectionFactoryConfiguration = new ConnectionFactoryConfiguration();

		context.setConnectionFactoryConfiguration(connectionFactoryConfiguration);

		Properties attributes = parseAttributes(node);
		String type = attributes.getProperty("type");

		if (stringHasValue(type)) {
			connectionFactoryConfiguration.setConfigurationType(type);
		}

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);

			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if ("property".equals(childNode.getNodeName())) {
				parseProperty(connectionFactoryConfiguration, childNode);
			}
		}
	}

	/**
	 * This method resolve a property from one of the three sources: system properties,
	 * properties loaded from the &lt;properties&gt; configuration element, and
	 * "extra" properties that may be supplied by the Maven or Ant environments.
	 *
	 * <p>If there is a name collision, system properties take precedence, followed by
	 * configuration properties, followed by extra properties.
	 *
	 * @param key property key
	 * @return the resolved property.  This method will return null if the property is
	 *     undefined in any of the sources.
	 */
	private String resolveProperty(String key) {
		String property = null;

		property = System.getProperty(key);

		if (property == null) {
			property = configurationProperties.getProperty(key);
		}

		if (property == null) {
			property = extraProperties.getProperty(key);
		}

		return property;
	}
}
