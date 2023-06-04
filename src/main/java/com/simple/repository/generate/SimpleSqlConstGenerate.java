package com.simple.repository.generate;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.util.SimpleFileUtils;
import com.simple.repository.util.SimpleStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * sql常量生成
 *
 * @author laiqx
 * @date 2023-04-23
 */

public class SimpleSqlConstGenerate {

    private static SimpleConfig config;

    public static Map<String, String> sqlMap = new HashMap<>();

    public void run() throws IOException {
        config = SimpleConfig.initConfig();
        analysisDoc();
    }

    private static void analysisDoc() throws IOException {
        String path = "src/main/java/" + config.sqlIndexPackage.replaceAll("\\.", "/");
        SimpleFileUtils.createDirectory(path);
        Map<String, String> sqlPathMap = getSqlFile();
        for (String key : sqlPathMap.keySet()) {
            // 解析文档中的sql语句
            analysisDocContent(sqlPathMap.get(key));
            // 创建sql索引文件
            String fileTag = SimpleStringUtils.underlineToHump(key);
            String fileContent = createConstContent(config.sqlIndexPackage, fileTag);
            String sqlIndexPath = SimpleFileUtils.relativeToAbsolutePath(path) + "\\" + fileTag + "Index.java";
            SimpleFileUtils.createFile(sqlIndexPath, fileContent);
        }
    }


    /**
     * 解析文档内容
     * <p>
     * 将文档的sql语句处理后保存到sqlMap中
     * 1.通过-- 和; 截取出每个sql语句的名称和sql
     * 2.并将sql名称转为常量命名
     * </p>
     *
     * @param docPath 文档路径
     */
    private static void analysisDocContent(String docPath) throws IOException {
        // 初始化容器
        sqlMap = new HashMap<>();
        // 读取文档内容后进行解析
        String data = new String(Files.readAllBytes(Paths.get(docPath)));
        String[] sqlArray = data.split(";");
        for (String sqlModel : sqlArray) {
            sqlModel = sqlModel.substring(sqlModel.indexOf("@name") + 5);
            Integer index = sqlModel.contains("\r\n") ? sqlModel.indexOf("\r\n") : sqlModel.indexOf("\n");
            String name = sqlModel.substring(0, index);
            name = SimpleStringUtils.humpToUnderline(name).trim();
            String sql = sqlModel.substring(sqlModel.indexOf("*/") + 2);
            for (int i = 0; i < 10; i++) {
                name = name.replaceAll("  ", " ");
                sql = sql.replaceAll("  ", " ");
            }
            sqlMap.put(name, sql);
        }
    }

    /**
     * 创建索引文件内容
     *
     * @param packageName 包名
     * @param fileTag     sql模板文件名
     * @return 返回内容
     */
    private static String createConstContent(String packageName, String fileTag) {
        String className = fileTag + "Index";
        String doc = "package " + packageName + ";\n" +
                "\n" +
                "public class " + className + " {\n" +
                "   %s \n" +
                "}\n";
        String template = "\n   public static final String %s = \"%s\";\n";
        StringBuilder text = new StringBuilder();
        for (String key : sqlMap.keySet()) {
            String value = SimpleStringUtils.underlineToHump(key);
            value = fileTag + "." + value.substring(0, 1).toLowerCase() + value.substring(1);
            text.append(String.format(template, key.toUpperCase(), value));
        }
        doc = String.format(doc, text);
        return doc;
    }

    /**
     * 获取resources目录下的sql模板文件
     *
     * @return 返回
     */
    private static Map<String, String> getSqlFile() throws IOException {
        // 获取目录下的所有文件
        File directory = new File("src/main/resources/sql");
        if (directory.exists() || directory.isDirectory()) {
            File[] files = directory.listFiles();
            Map<String, String> map = new HashMap<>();
            assert files != null;
            for (File file : files) {
                if (file.getName().contains(".sql")) {
                    String name = file.getName().toLowerCase().replaceAll(".sql", "");
                    String filePath = file.getCanonicalPath();
                    map.put(name, filePath);
                }
            }
            return map;
        }
        return new HashMap<>();
    }

}
