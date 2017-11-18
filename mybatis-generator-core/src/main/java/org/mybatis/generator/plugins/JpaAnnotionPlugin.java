package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * @desc 
 * @author zbss
 * @date 2017/11/18 18:12
 */
public class JpaAnnotionPlugin extends PluginAdapter {

    /**
     * 处理实体类的包和@Table注解
     * @param topLevelClass
     * @param introspectedTable
     */
    private void addJpaAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 给类添加JPA注解
        topLevelClass.addImportedType("javax.persistence.*");
        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        topLevelClass.addAnnotation("@Table(name = \"" + tableName + "\")");

        List<Field> fields = topLevelClass.getFields();
        if (fields == null || fields.isEmpty()){
            return;
        }

        // 给字段添加JPA注解
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        for (Field field : fields){
            for (IntrospectedColumn column : introspectedColumns){
                if (column.getJavaProperty().equals(field.getName())){
                    field.addAnnotation("@Column(name=\""+column.getActualColumnName()+"\")");
                    if (column.isIdentity()){
                        field.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY)");
                    }
                }
            }
        }
    }

    /**
     * 生成基础实体类
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addJpaAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * 生成实体类注解KEY对象
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addJpaAnnotation(topLevelClass, introspectedTable);
        return true;
    }

    /**
     * 生成带BLOB字段的对象
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addJpaAnnotation(topLevelClass, introspectedTable);
        return false;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
