package com.simple.repository.config;

import com.simple.repository.generate.ModelInfo;
import com.simple.repository.master.exception.SimpleException;
import com.simple.repository.util.SimpleStringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * 仓储配置
 *
 * @author laiqx
 * @date 2023-02-08
 */
public class SimpleConfig {

    /**
     * 运行环境
     */
    private static String runEnv;

    /**
     * 环境配置
     */
    private Map<String, LinkedHashMap<String, ?>> evnConfigTemp = new HashMap<>();

    /**
     * 环境标识
     */
    private static final List<String> ENV_TAGS = Arrays.asList("dev", "test", "pre", "pro");


    /**
     * 是否spring环境
     */
    public boolean springEnv = false;

    /**
     * 是否输出日志
     */
    public boolean log = false;

    /**
     * 是否缓存
     */
    public boolean isCache = false;

    /**
     * sql索引目录
     */
    public String sqlIndexPackage;


    /**
     * 缓存表
     */
    public List<String> cacheTable = new ArrayList<>();

    /**
     * 数据源
     */
    public DataSource dataSource;

    /**
     * redis 配置
     */
    public Redis redis;

    /**
     * entity 创建的信息
     */
    public List<ModelInfo> ModelInfos = new ArrayList<>();

    private static SimpleConfig config = null;

    private SimpleConfig() {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("simple-repository.yml");
        Yaml yaml = new Yaml();
        analysisYml(yaml.load(in));
        loadEnvConfig();
    }

    /**
     * 初始化配置
     *
     * @return 返回配置对象
     */
    public static SimpleConfig initConfig() throws IOException {
        if (null == config) {
            config = new SimpleConfig();
        }
        return config;
    }

    /**
     * 设置运行环境
     *
     * @param args 系统参数
     */
    public static void setRunEnv(String[] args) {
        String simpleEnv = "--simple.env=";
        for (String arg : args) {
            if (arg.indexOf(simpleEnv) == 0) {
                SimpleConfig.runEnv = arg.replace(simpleEnv, "").trim();
            }
        }
    }


    /**
     * 分析simple的yml配置
     *
     * @param map 所有配置项
     */
    private void analysisYml(LinkedHashMap<String, Object> map) {
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            if (key.equals("env") && null == runEnv && ENV_TAGS.contains(obj.toString())) {
                runEnv = obj.toString();
            } else if (key.equals("cache-table")) {
                cacheTable = Arrays.asList(obj.toString().split("\\|"));
                isCache = !cacheTable.isEmpty();
            } else if (key.equals("sql-index-package")) {
                sqlIndexPackage = obj.toString();
            } else if (ENV_TAGS.contains(key)) {
                evnConfigTemp.put(key, (LinkedHashMap<String, ?>) obj);
            } else if (key.equals("generate")) {
                analysisModelInfo((LinkedHashMap<String, Object>) obj);
            } else if (obj instanceof LinkedHashMap) {
                analysisYml((LinkedHashMap<String, Object>) obj);
            }
        }
    }

    /**
     * 加载环境配置
     */
    private void loadEnvConfig() {
        LinkedHashMap<String, ?> map = evnConfigTemp.get(runEnv);
        if (null == map) {
            throw new SimpleException(String.format("请正确配置数据源:%s环境无配置", runEnv));
        }
        //加载日志配置
        log = (Boolean) map.get("log");
        // 加载数据库配置
        LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) map.get("datasource");
        if (null != dataMap) {
            dataSourceConfig(transition(dataMap.get("url"), String.class), transition(dataMap.get("username"), String.class),
                    transition(dataMap.get("password"), String.class), transition(dataMap.get("driver"), String.class));
        }
        // 加载redis配置
        dataMap = (LinkedHashMap<String, Object>) map.get("redis");
        if (null != dataMap) {
            redisConfig(transition(dataMap.get("enable"), Boolean.class), transition(dataMap.get("database"), Integer.class),
                    transition(dataMap.get("host"), String.class), transition(dataMap.get("port"), Integer.class), transition(dataMap.get("password"), String.class));
        }
        evnConfigTemp = new HashMap<>();
    }

    /**
     * 分析获取代码生成的配置
     *
     * @param map yml配置
     */
    public void analysisModelInfo(LinkedHashMap<String, Object> map) {
        String packageName = "";
        String schema = "";
        for (String key : map.keySet()) {
            if (null == map.get(key)) {
                continue;
            }
            Object obj = map.get(key);
            switch (key) {
                case "spring":
                    springEnv = (boolean) obj;
                    break;
                case "package":
                    packageName = obj.toString();
                    break;
                case "schema":
                    schema = obj.toString();
                    break;
                case "tables":
                    String tables = obj.toString().replace(" ", "");
                    if (SimpleStringUtils.isNotEmpty(tables)) {
                        List<String> tableList = Arrays.asList(tables.split("\\|"));
                        ModelInfos.add(new ModelInfo(schema, packageName, tableList));
                    }
                    break;
            }
            if (obj instanceof LinkedHashMap) {
                analysisModelInfo((LinkedHashMap<String, Object>) obj);
            }
        }
    }

    /**
     * 数据源配置
     *
     * @param url      地址
     * @param username 账号
     * @param password 密码
     * @param driver   驱动名称
     */
    private void dataSourceConfig(String url, String username, String password, String driver) {
        dataSource = new DataSource();
        dataSource.url = url;
        dataSource.username = username;
        dataSource.password = password;
        dataSource.driver = driver;
    }

    /**
     * 设置redis配置
     *
     * @param enable   是否开启redis
     * @param database 使用的仓库
     * @param host     地址
     * @param port     端口
     * @param password 密码
     */
    private void redisConfig(Boolean enable, Integer database, String host, Integer port, String password) {
        redis = new Redis();
        redis.enable = enable;
        redis.database = database;
        redis.host = host;
        redis.port = port;
        redis.password = password;
    }

    /**
     * 数据源配置
     */
    public class DataSource {
        private String url;

        private String username;

        private String password;

        private String driver;

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDriver() {
            return driver;
        }
    }

    /**
     * redis配置类
     */
    public class Redis {
        private Boolean enable = false;

        private Integer database = 0;

        private String host;

        private Integer port;

        private String password;

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public Integer getDatabase() {
            return database;
        }

        public void setDatabase(Integer database) {
            this.database = database;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    /**
     * 类型转换
     *
     * @param source 值来源
     * @param tClass 类型
     * @param <T>    返回的类型
     * @return 返回值
     */
    private <T> T transition(Object source, Class<T> tClass) {
        if (null == source || SimpleStringUtils.isEmpty(source.toString())) {
            return null;
        }
        String value = source.toString();
        if (tClass.equals(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (tClass.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        }
        return (T) value;
    }


}
