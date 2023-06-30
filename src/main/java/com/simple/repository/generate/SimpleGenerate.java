package com.simple.repository.generate;


import com.simple.repository.config.SimpleConfig;
import com.simple.repository.connect.SimpleSession;
import com.simple.repository.util.SimpleFileUtils;
import com.simple.repository.util.SimpleSqlDataType;
import com.simple.repository.util.SimpleStringUtils;

import java.io.IOException;
import java.util.*;

/**
 * repository生成器
 */
public class SimpleGenerate {

    private static SimpleConfig config;
    private static SimpleSession simpleSession;
    private static final List<String> notCreateField = Arrays.asList("id");


    public SimpleGenerate() throws IOException{
        config = SimpleConfig.initConfig();
        simpleSession = SimpleSession.openSession();
    }

    /**
     * 运行代码生成器
     * @throws IOException 代码生成器异常
     */
    public void run() throws IOException {
        analysisModel();
    }

    /**
     * 分析数据表模型
     * @throws IOException 读取文件异常
     */
    public void analysisModel() throws IOException {
        for (ModelInfo info : config.ModelInfos) {
            String path = "src/main/java/" + info.packageName.replaceAll("\\.", "/");
            String directory = SimpleFileUtils.relativeToAbsolutePath(path);
            SimpleFileUtils.createDirectory(directory);
            for (String table : info.tables) {
                createModelFile(path, info.packageName, info.schema, table);
            }
        }
    }

    /**
     * 创建数据模型文件
     * @param path 文件路径
     * @param packageName 包名
     * @param schema 数据库前缀
     * @param table 表名
     * @throws IOException 文件创建异常
     */
    public void createModelFile(String path, String packageName, String schema, String table) throws IOException {
        String sql = String.format("select column_name,data_type,column_comment from information_schema.columns where table_schema='%s' and  table_name = '%s'", schema, table);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = simpleSession.query(sql);
        } catch (Exception e) {
            System.out.println("repository生成器:生成表对应数据失败，table=" + table + "原因=" + e.getMessage());
            return;
        }
        List<TableField> fields = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String fieldName = map.get("column_name").toString();
            String type = map.get("data_type").toString();
            String remark = map.get("column_comment").toString();
            fields.add(new TableField(fieldName, sqlTypeMatch(type), remark));
        }
        String className = SimpleStringUtils.underlineToHump(table) + "Entity";
        // 创建entity
        createEntity(path, packageName, table, className, fields);
        // 创建Repository
        createRepository(path, packageName, className);
        System.out.println("repository生成器:创建表成功，table=" + table);
    }

    /**
     * 创建entity文件
     * @param path 路径
     * @param packageName 包名
     * @param table 表名
     * @param className 类名
     * @param fields 字段集合
     * @throws IOException 文件创建失败异常
     */
    private void createEntity(String path, String packageName,String table, String className, List<TableField> fields) throws IOException {
        String idType = "Long";
        List<String> typeList = new ArrayList<>();
        for (TableField field : fields) {
            if ("id".equals(field.fieldName)) {
                idType = field.type;
            }
            typeList.add(field.type);
        }
        String doc = "package " + packageName + ";\n" +
                "\n" +
                createImport(typeList) +
                "public class " + className + " extends Entity<" + idType + "> {\n" +
                "@params\n" +
                "    public Class<?> getIdentityClass(){\n" +
                "        return " + idType + ".class;\n" +
                "    }\n\n" +
                "@getAndSet\n" +
                "@fieldConst" +
                "}";
        String params = createParams(fields);
        String getAndSet = "";
        String fieldConst = createFieldConst(fields);
        // 如果设置为私有属性则创建private访问符字段并加上get和set
        if(config.privateProperty){
            params = params.replaceAll("public","private");
            getAndSet = createGetAndSet(fields);
        }
        // 替换生成的字符
        doc = doc.replaceAll("@params", params);
        doc = doc.replaceAll("@getAndSet", getAndSet);
        doc = doc.replaceAll("@fieldConst", fieldConst);
        // 输出类文件
        String filePath = SimpleFileUtils.relativeToAbsolutePath(path) + "\\" + className + ".java";
        SimpleFileUtils.createFile(filePath, doc);
    }

    private String createImport(Collection<String> types) {
        String str = "import com.simple.repository.master.Entity;\n";
        str += types.contains("Date") ? "import java.util.Date;\n" : "";
        str += types.contains("BigDecimal") ? "import java.math.BigDecimal;\n" : "";
        str += types.contains("Timestamp") ? "import java.sql.Timestamp;;\n" : "";
        return str + "\n";
    }

    /**
     * 创建仓储管理类
     * <p>
     * 判断spring配置是否开启，开启则使用Repository注解
     * </p>
     * @param entityPath 路径
     * @param entityPackageName 包名
     * @param className 类名
     * @throws IOException 文件创建异常
     */
    public void createRepository(String entityPath, String entityPackageName, String className) throws IOException {
        // 生成类的java字符串
        String packageName = entityPackageName.substring(0, entityPackageName.lastIndexOf("."));
        String repositoryClassName = className.replace("Entity", "") + "Repository";
        String doc = "package " + packageName + ";\n" +
                "\n" +
                "import " + entityPackageName + "." + className + ";\n" +
                "import com.simple.repository.master.BaseRepository;\n" +
                (config.springEnv ? "import org.springframework.stereotype.Repository;\n\n@Repository\n" : "\n") +
                "public class " + repositoryClassName + " extends BaseRepository<" + className + "> {\n\n" +
                "    public " + repositoryClassName + "(){\n" +
                "        this.setEntityClass(" + className + ".class);\n" +
                "    }\n\n" +
                "}\n";
        // 创建java文件,如果已存在则不创建
        String path = entityPath.substring(0, entityPath.lastIndexOf("/"));
        String filePath = SimpleFileUtils.relativeToAbsolutePath(path) + "\\" + repositoryClassName + ".java";
        if (!SimpleFileUtils.exists(filePath)) {
            SimpleFileUtils.createFile(filePath, doc);
        }
    }

    /**
     * 创建get/set文本
     *
     * @param fields 字段列表
     * @return 返回get/set文本
     */
    public String createGetAndSet(List<TableField> fields) {
        StringBuilder text = new StringBuilder();
        for (TableField field : fields) {
            if (notCreateField.contains(field.fieldName)) {
                continue;
            }
            String getAndSet = "    public @type get@upField() {return @field;}\n\n" +
                    "    public void set@upField(@type @field) {this.@field = @field;}\n\n";
            String upperFieldName = SimpleStringUtils.underlineToHump(field.fieldName);
            String fieldName = upperFieldName.substring(0, 1).toLowerCase() + upperFieldName.substring(1);
            getAndSet = getAndSet.replaceAll("@upField", upperFieldName);
            getAndSet = getAndSet.replaceAll("@field", fieldName);
            getAndSet = getAndSet.replaceAll("@type", field.type);
            text.append(getAndSet);
        }
        return text.toString();
    }

    /**
     * 创建entity字段
     *
     * @param fields 字段列
     * @return 返回字段文本
     */
    public String createParams(List<TableField> fields) {
        StringBuilder text = new StringBuilder();
        for (TableField field : fields) {
            if (notCreateField.contains(field.fieldName)) {
                continue;
            }
            String remark = "\n    /**\n" +
                    "     * " + field.comment + "\n" +
                    "     */\n";
            String param = remark + "    public @type @field;\n";
            String upperFieldName = SimpleStringUtils.underlineToHump(field.fieldName);
            String fieldName = upperFieldName.substring(0, 1).toLowerCase() + upperFieldName.substring(1);
            param = param.replaceAll("@field", fieldName);
            param = param.replaceAll("@type", field.type);
            text.append(param);
        }
        return text.toString();
    }

    /**
     * 创建字段常量
     *
     * @param fields 字段常量
     * @return 返回字段常量
     */
    public String createFieldConst(List<TableField> fields) {
        StringBuilder text = new StringBuilder();
        for (TableField field : fields) {
            if (notCreateField.contains(field.fieldName)) {
                continue;
            }
            String param = "    public static final String @name = \"@value\";\n\n";
            param = param.replaceAll("@name", field.fieldName.toUpperCase());
            param = param.replaceAll("@value", field.fieldName);
            text.append(param);
        }
        return text.toString();
    }

    /**
     * 获取字段类型
     *
     * @param sqlType 数据库类型
     * @return 返回java 类型
     */
    public String sqlTypeMatch(String sqlType) {
        SimpleSqlDataType.SqlType type = SimpleSqlDataType.MYSQL_TYPE.get(sqlType.toUpperCase());
        return null == type ? SimpleSqlDataType.SqlType.STRING.getTypeName() : type.getTypeName();
    }

}

