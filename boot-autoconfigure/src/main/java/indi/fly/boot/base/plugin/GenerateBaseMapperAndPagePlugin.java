package indi.fly.boot.base.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Iterator;
import java.util.List;

public class GenerateBaseMapperAndPagePlugin extends PluginAdapter {

    public GenerateBaseMapperAndPagePlugin(){}

    public boolean validate(List warnings) {
        return true;
    }

    public boolean clientGenerated(Interface face, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        face.getSuperInterfaceTypes().clear();
        face.getImportedTypes().clear();
        String rootInterface = context.getJavaClientGeneratorConfiguration().getProperty("rootInterface");
        String repository = context.getJavaClientGeneratorConfiguration().getProperty("repository");
        if(rootInterface != null) {

            String pk = "Object";
            List primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            if(primaryKeyColumns != null && primaryKeyColumns.size() == 1) {
                for(Iterator iterator = primaryKeyColumns.iterator(); iterator.hasNext();) {
                    IntrospectedColumn keyColumn = (IntrospectedColumn)iterator.next();
                    pk = keyColumn.getFullyQualifiedJavaType().getShortName();
                }

            }
            FullyQualifiedJavaType baseMapper = new FullyQualifiedJavaType((new StringBuilder()).append("BaseMapper<").append(introspectedTable.getFullyQualifiedTable().getDomainObjectName()).append(",").append(pk).append(">").toString());
            face.addImportedType(new FullyQualifiedJavaType(rootInterface));
            face.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
            face.addSuperInterface(baseMapper);
        }
        if(repository != null) {
            face.addImportedType(new FullyQualifiedJavaType(repository));
            face.getAnnotations().add("@Repository");
        }
        face.getMethods().clear();
        return true;
    }

    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
        XmlElement parentElement = document.getRootElement();
        XmlElement sql = new XmlElement("sql");
        sql.addAttribute(new Attribute("id", "queryConditionList"));
        XmlElement topIf = new XmlElement("if");

        topIf.addAttribute(new Attribute("test", "qcList != null"));
        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("index", "index"));
        foreach.addAttribute(new Attribute("collection", "qcList"));
        foreach.addAttribute(new Attribute("open", ""));
        foreach.addAttribute(new Attribute("separator", ""));
        foreach.addAttribute(new Attribute("close", ""));
        XmlElement choose = new XmlElement("choose");
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.relation == 'and'"));
        when.addElement(new TextElement("and"));
        choose.addElement(when);
        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(new TextElement("or"));
        choose.addElement(otherwise);
        foreach.addElement(choose);
        choose = new XmlElement("choose");
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'eq'"));
        when.addElement(new TextElement("${item.field} = #{item.vList[0]}"));
        choose.addElement(when);
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'gt'"));
        when.addElement(new TextElement("${item.field} &gt;= #{item.vList[0]}"));
        choose.addElement(when);
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'lt'"));
        when.addElement(new TextElement("${item.field} &lt;= #{item.vList[0]}"));
        choose.addElement(when);
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'like'"));
        when.addElement(new TextElement("${item.field} like concat('%',#{item.vList[0]},'%')"));
        choose.addElement(when);
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'in'"));
        when.addElement(new TextElement("${item.field} in"));
        XmlElement foreach2 = new XmlElement("foreach");
        foreach2.addAttribute(new Attribute("item", "in_item"));
        foreach2.addAttribute(new Attribute("index", "in_index"));
        foreach2.addAttribute(new Attribute("collection", "item.vList"));
        foreach2.addAttribute(new Attribute("open", "("));
        foreach2.addAttribute(new Attribute("separator", ","));
        foreach2.addAttribute(new Attribute("close", ")"));
        when.addElement(foreach2);
        choose.addElement(when);
        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "item.condition == 'range'"));
        when.addElement(new TextElement("${item.field} &lt;= #{item.vList[1]} and ${item.field} &gt;= #{item.vList[0]}"));
        choose.addElement(when);
        foreach.addElement(choose);
        topIf.addElement(foreach);
        sql.addElement(topIf);
        parentElement.addElement(sql);

        sql = new XmlElement("sql");
        sql.addAttribute(new Attribute("id", "queryOrderList"));
        topIf = new XmlElement("if");
        topIf.addAttribute(new Attribute("test", "qoList != null and qoList.size() != 0"));
        foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("index", "index"));
        foreach.addAttribute(new Attribute("collection", "qoList"));
        foreach.addAttribute(new Attribute("open", ""));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addAttribute(new Attribute("close", ""));
        XmlElement inIf = new XmlElement("if");
        inIf.addAttribute(new Attribute("test", "item.orderType == 'desc' or item.orderType == 'DESC'"));
        inIf.addElement(new TextElement("${item.orderField} desc"));
        foreach.addElement(inIf);
        inIf = new XmlElement("if");
        inIf.addAttribute(new Attribute("test", "item.orderType == 'asc' or item.orderType == 'ASC'"));
        inIf.addElement(new TextElement("${item.orderField} asc"));
        foreach.addElement(inIf);
        topIf.addElement(foreach);
        sql.addElement(topIf);

        topIf = new XmlElement("if");
        topIf.addAttribute(new Attribute("test", "qoList == null or qoList.size() == 0"));
        topIf.addElement(new TextElement("update_time desc"));
        sql.addElement(topIf);
        parentElement.addElement(sql);

        XmlElement list = new XmlElement("select");
        list.addAttribute(new Attribute("id", "list"));
        list.addAttribute(new Attribute("resultMap", "BaseResultMap"));

        list.addElement(new TextElement("select"));
        XmlElement baseColumnList = new XmlElement("include");
        baseColumnList.addAttribute(new Attribute("refid", "Base_Column_List"));
        list.addElement(baseColumnList);
        list.addElement(new TextElement((new StringBuilder()).append("from " + tableName + " where 1 = 1").toString()));
        XmlElement qcInclude = new XmlElement("include");
        qcInclude.addAttribute(new Attribute("refid", "queryConditionList"));
        list.addElement(qcInclude);
        list.addElement(new TextElement("order by"));
        XmlElement qoInclude = new XmlElement("include");
        qoInclude.addAttribute(new Attribute("refid", "queryOrderList"));
        list.addElement(qoInclude);
        list.addElement(new TextElement("limit #{pageNo}, #{pageSize}"));
        parentElement.addElement(list);

        XmlElement countList = new XmlElement("select");
        countList.addAttribute(new Attribute("id", "countList"));
        countList.addAttribute(new Attribute("resultType", "java.lang.Integer"));
        countList.addElement(new TextElement("select count(*) from " + tableName + " where 1 = 1"));
        countList.addElement(qcInclude);
        parentElement.addElement(countList);

        return true;
    }
}
