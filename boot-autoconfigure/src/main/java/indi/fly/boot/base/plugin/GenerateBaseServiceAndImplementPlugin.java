package indi.fly.boot.base.plugin;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;

import java.beans.Introspector;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenerateBaseServiceAndImplementPlugin extends PluginAdapter {
    private String serviceTargetDir;
    private String serviceTargetPackage;
    private String service;
    private boolean overwrite;
    private ShellCallback shellCallback = new DefaultShellCallback(true);

    public GenerateBaseServiceAndImplementPlugin() {
    }

    public boolean validate(List<String> warnings) {
        this.serviceTargetDir = this.properties.getProperty("targetProject");
        this.serviceTargetPackage = this.properties.getProperty("targetPackage");
        this.service = this.properties.getProperty("service");
        this.overwrite = Boolean.valueOf(this.properties.getProperty("overwrite")).booleanValue();
        return StringUtility.stringHasValue(this.serviceTargetDir) && StringUtility.stringHasValue(this.serviceTargetPackage) && StringUtility.stringHasValue(this.service);
    }

    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> javaFiles = new ArrayList();
        Iterator var3 = introspectedTable.getGeneratedJavaFiles().iterator();

        while(true) {
            String shortName;
            do {
                if (!var3.hasNext()) {
                    return javaFiles;
                }

                GeneratedJavaFile javaFile = (GeneratedJavaFile)var3.next();
                CompilationUnit unit = javaFile.getCompilationUnit();
                FullyQualifiedJavaType baseModelJavaType = unit.getType();
                shortName = baseModelJavaType.getShortName();
            } while(!shortName.endsWith("Mapper"));

            String serviceInterfaceFullName = this.serviceTargetPackage + "." + shortName.replace("Mapper", "Service");
            Interface serviceInterface = new Interface(serviceInterfaceFullName);
            serviceInterface.setVisibility(JavaVisibility.PUBLIC);
            String pk = "Object";
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            IntrospectedColumn keyColumn;
            if (primaryKeyColumns != null && primaryKeyColumns.size() == 1) {
                for(Iterator var12 = primaryKeyColumns.iterator(); var12.hasNext(); pk = keyColumn.getFullyQualifiedJavaType().getShortName()) {
                    keyColumn = (IntrospectedColumn)var12.next();
                }
            }

            String rootInterface = this.properties.getProperty("rootInterface");
            String implServicePkg = this.serviceTargetPackage + ".impl";
            TopLevelClass topLevelClass = new TopLevelClass(new FullyQualifiedJavaType(implServicePkg + "." + shortName.replace("Mapper", "ServiceImpl")));
            topLevelClass.setVisibility(JavaVisibility.PUBLIC);
            topLevelClass.addImportedType(this.service);
            topLevelClass.addImportedType(serviceInterfaceFullName);
            topLevelClass.addSuperInterface(new FullyQualifiedJavaType(serviceInterfaceFullName));
            topLevelClass.getAnnotations().add("@Service");
            if (rootInterface != null) {
                FullyQualifiedJavaType baseService = new FullyQualifiedJavaType("BaseService<" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + "," + pk + ">");
                serviceInterface.addImportedType(new FullyQualifiedJavaType(rootInterface));
                serviceInterface.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
                serviceInterface.addSuperInterface(baseService);
                FullyQualifiedJavaType baseServiceImpl = new FullyQualifiedJavaType("BaseServiceImpl<" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + "," + pk + ">");
                topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
                topLevelClass.addImportedType(rootInterface.replace("BaseService", "BaseServiceImpl"));
                topLevelClass.setSuperClass(baseServiceImpl);
            }

            String restPkg = this.serviceTargetPackage.replace(".service", ".rest");
            TopLevelClass restClass = new TopLevelClass(new FullyQualifiedJavaType(restPkg + "." + shortName.replace("Mapper", "RestApi")));
            restClass.setVisibility(JavaVisibility.PUBLIC);
            restClass.addImportedType("org.springframework.stereotype.Controller");
            restClass.addImportedType("javax.ws.rs.*");
            restClass.addImportedType("javax.ws.rs.core.MediaType");
            restClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
            restClass.addImportedType("javax.ws.rs.core.MediaType");
            restClass.addImportedType("io.swagger.annotations.*");
            restClass.addImportedType("indi.fly.boot.base.model.PageModel");
            restClass.addImportedType("indi.fly.boot.base.model.result.*");
            restClass.addImportedType("indi.fly.boot.base.model.search.*");
            restClass.addImportedType("indi.fly.boot.base.util.*");
            restClass.addImportedType("indi.fly.boot.base.util.JsonUtils");
            restClass.addImportedType("com.google.gson.reflect.TypeToken");
            restClass.addImportedType("java.util.List");
            restClass.addImportedType("javax.validation.Valid");
            restClass.addImportedType(serviceInterfaceFullName);
            restClass.addImportedType(introspectedTable.getBaseRecordType());
            restClass.getAnnotations().add("@Controller");
            String path = Introspector.decapitalize(shortName.replace("Mapper", ""));
            restClass.getAnnotations().add("@Path(\"" + path + "\")");
            restClass.getAnnotations().add("@Produces(MediaType.APPLICATION_JSON)");
            restClass.getAnnotations().add("@Api(\"" + shortName.replace("Mapper", "RestApi") + "\")");
            restClass.getAnnotations().add("@ApiImplicitParams({@ApiImplicitParam(paramType = \"header\", dataType = \"string\", name = \"Authorization\")})\n");
            String xService = Introspector.decapitalize(shortName.replace("Mapper", "Service"));
            Field field = new Field(xService, new FullyQualifiedJavaType(serviceInterfaceFullName));
            field.getAnnotations().add("@Autowired");
            field.setVisibility(JavaVisibility.PRIVATE);
            restClass.getFields().add(field);
            this.addBaseMethod(introspectedTable, restClass, xService, pk);

            try {
                JavaFormatter javaFormatter = this.context.getJavaFormatter();
                this.checkAndAddJavaFile(new GeneratedJavaFile(serviceInterface, this.serviceTargetDir, javaFormatter), javaFiles, this.serviceTargetPackage);
                this.checkAndAddJavaFile(new GeneratedJavaFile(topLevelClass, this.serviceTargetDir, javaFormatter), javaFiles, implServicePkg);
                this.checkAndAddJavaFile(new GeneratedJavaFile(restClass, this.serviceTargetDir, javaFormatter), javaFiles, restPkg);
            } catch (ShellException var21) {
                var21.printStackTrace();
            }
        }
    }

    private void checkAndAddJavaFile(GeneratedJavaFile javaFile, List<GeneratedJavaFile> javaFiles, String pkg) throws ShellException {
        File dir = this.shellCallback.getDirectory(this.serviceTargetDir, pkg);
        File file = new File(dir, javaFile.getFileName());
        if (file.exists()) {
            if (this.overwrite) {
                javaFiles.add(javaFile);
            }
        } else {
            javaFiles.add(javaFile);
        }

    }

    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        try {
            java.lang.reflect.Field field = sqlMap.getClass().getDeclaredField("isMergeable");
            field.setAccessible(true);
            field.setBoolean(sqlMap, !this.overwrite);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return true;
    }

    private void addBaseMethod(IntrospectedTable introspectedTable, TopLevelClass restClass, String xService, String pk) {
        String bean = Introspector.decapitalize(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        String Bean = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        Method listByPage = new Method("list");
        listByPage.getAnnotations().add("@POST");
        listByPage.getAnnotations().add("@Path(\"list\")");
        listByPage.getAnnotations().add("@ApiOperation(\"列表\")");
        listByPage.setVisibility(JavaVisibility.PUBLIC);
        listByPage.setReturnType(new FullyQualifiedJavaType("RestResp<RestData<" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">>"));
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("@ApiParam(value = \"过滤条件\") @FormParam(\"queryCondition\") String"), "queryCondition\n                                           ");
        listByPage.getParameters().add(parameter);
        parameter = new Parameter(new FullyQualifiedJavaType("@ApiParam(value = \"排序条件\") @FormParam(\"queryOrder\") String"), "queryOrder\n                                           ");
        listByPage.getParameters().add(parameter);
        parameter = new Parameter(new FullyQualifiedJavaType("@Valid @BeanParam PageModel"), "pageModel");
        listByPage.getParameters().add(parameter);
        listByPage.getBodyLines().add("");
        listByPage.getBodyLines().add("List<QueryCondition> qcList = JsonUtils.fromJson(queryCondition, new TypeToken<List<QueryCondition>>() {}.getType());");
        listByPage.getBodyLines().add("List<QueryCondition> qoList = JsonUtils.fromJson(queryOrder, new TypeToken<List<QueryOrder>>() {}.getType());\n");
        listByPage.getBodyLines().add("RestData rs = " + xService + ".list(qcList, qoList, pageModel);");

        listByPage.getBodyLines().add("return new RestResp<>(rs);");
        restClass.getMethods().add(listByPage);

        Method get = new Method("get");
        get.getAnnotations().add("@GET");
        get.getAnnotations().add("@Path(\"get/{id}\")");
        get.getAnnotations().add("@ApiOperation(\"详情\")");
        get.setVisibility(JavaVisibility.PUBLIC);
        get.setReturnType(new FullyQualifiedJavaType("RestResp<" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") " + pk + ""), "id");
        get.getParameters().add(parameter);
        get.getBodyLines().add("return new RestResp<>(" + xService + ".getByPrimaryKey(id));");
        restClass.getMethods().add(get);
        Method add = new Method("add");
        add.getAnnotations().add("@POST");
        add.getAnnotations().add("@Path(\"add\")");
        add.getAnnotations().add("@ApiOperation(\"新增\")");
        add.setVisibility(JavaVisibility.PUBLIC);
        add.setReturnType(new FullyQualifiedJavaType("RestResp<" + introspectedTable.getFullyQualifiedTable().getDomainObjectName() + ">"));
        parameter = new Parameter(new FullyQualifiedJavaType("@ApiParam(required = true)@FormParam(\"bean\") String"), "bean");
        add.getParameters().add(parameter);
        add.getBodyLines().add("" + Bean + " " + bean + " = JsonUtils.fromJson(bean, " + Bean + ".class);");
        add.getBodyLines().add("BeanValidator.validate(" + bean + ");");
        add.getBodyLines().add("" + xService + ".saveSelective(" + bean + ");");
        add.getBodyLines().add("return new RestResp<>(" + bean + ");");
        restClass.getMethods().add(add);
        Method delete = new Method("delete");
        delete.getAnnotations().add("@DELETE");
        delete.getAnnotations().add("@Path(\"delete/{id}\")");
        delete.getAnnotations().add("@ApiOperation(\"删除\")");
        delete.setVisibility(JavaVisibility.PUBLIC);
        delete.setReturnType(new FullyQualifiedJavaType("RestResp"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") " + pk + ""), "id");
        delete.getParameters().add(parameter);
        delete.getBodyLines().add("" + xService + ".deleteByPrimaryKey(id);");
        delete.getBodyLines().add("return new RestResp<>();");
        restClass.getMethods().add(delete);
        Method update = new Method("update");
        update.getAnnotations().add("@PUT");
        update.getAnnotations().add("@Path(\"modify/{id}\")");
        update.getAnnotations().add("@ApiOperation(\"修改\")");
        update.setVisibility(JavaVisibility.PUBLIC);
        update.setReturnType(new FullyQualifiedJavaType("RestResp"));
        parameter = new Parameter(new FullyQualifiedJavaType("@PathParam(\"id\") " + pk + ""), "id\n                         ");
        update.getParameters().add(parameter);
        parameter = new Parameter(new FullyQualifiedJavaType("@ApiParam(required = true)@FormParam(\"bean\") String"), "bean");
        update.getParameters().add(parameter);
        update.getBodyLines().add("" + Bean + " " + bean + " = JsonUtils.fromJson(bean, " + Bean + ".class);");
        update.getBodyLines().add("" + bean + ".setId(id);");
        update.getBodyLines().add("BeanValidator.validate(" + bean + ");");
        update.getBodyLines().add("" + xService + ".updateByPrimaryKeySelective(" + bean + ");");
        update.getBodyLines().add("return new RestResp<>();");
        restClass.getMethods().add(update);
    }
}
