# Simple-Repository
Simple-Repository是一款简易的数据存储管理框架，提供便捷的缓存管理、数据库连接与数据持久化、代码生成等功能。

#### <a href="http://gpl.edlian.com/book/master/index.html" target="_blank" >点击跳转Simple-Repository文档</a>

# 版本更新
### Version_1.1.0
#### 1.Entity生成新增TABLE常量标识来源表名
#### 2.Condition生成Sort容器,用于Condition查询排序
#### 3.SimpleCollectionUtil新增listValueGroupMap与listGroupMap方法，分别用于将list转为分组MAP(Map<K,List<V>>)

# 为什么要设计Simple-Repository
### 1. SQL从业务代码中完全解耦
日常工作中使用过Spring JPA、Hibernate、Mybatis、JOOQ等主流的ORM框架。但这些框架要么过于厚重配置繁琐，要么会将sql语句交织在业务代码中，
更有如JOOQ等直接将sql写在了Service里。这些框架要求开发人员具有一定sql基础，但即使有一定资历的开发者在编写复杂sql时，由于无法直观的查阅也会存在极大的不便和风险。
sql性能和安全几乎只能依靠开发者个人水平决定,而我更倾向于专业的人做专业的事。

那么能否将sql从代码中完全剥离、并且能够直观的审查甚至能加入DBA角色审查或编写sql呢？

可以的，这也是Simple-Repository要解决的事情，在编写完sql后邀请几个小伙伴在不用看业务代码情况下直观审查sql语句，甚至由专业的DBA编写复杂的sql再由开发人员调用。

### 2. 简化SQL书写优化效率
日常工作中大部分开发者会使用navicat等sql可视化管理工具中编写sql并校验完成后，再拷贝到项目代码中根据所使用的ORM框架格式进行改写，
这是比较多余且繁琐的步骤。而Simple-Repository提供.sql文件的编写方式，几乎可以直接在可视化管理工具中编辑，只需编辑完成后将待传参用“${}或&{}”替换符号。

### 3. 搭建一个数据存储
Simple-Repository并不仅仅是ORM框架而是数据仓库，未来将会加入更多数据相关的服务与支持。
例如解决数据缓存、分布式事务、数据的安全校验、数据分析与清洗等等。不过这些都是未来愿景，
目前仅支持分布式缓存管理。

# 使用方式
### 一.引入jar包

~~~

<dependency>
    <groupId>com.simple-repository</groupId>
    <artifactId>simple-data</artifactId>
    <version>1.0.1</version>
</dependency>

~~~

### 二.配置simple-repository.yml

~~~

simple:
  env: dev # 应用环境 dev|test|pre|pro
  dev:
    log: true # 是否输出日志
    datasource:
      url: jdbc:mysql://localhost:3306/simple?useUnicode=true&characterEncoding=utf8&useSSL=true
      username: root
      password: youthyo.com
      driver: com.mysql.cj.jdbc.Driver
    redis: # redis配置
      enable: true  # 是否开启redis缓存,false则使用本地缓存
      host: localhost
      port: 6379
      password: simple@123
  pro:
    log: true
    datasource:
      url: jdbc:mysql://localhost:3306/simple?useUnicode=true&characterEncoding=utf8&useSSL=true
      username: root
      password: youthyo.com
      driver: com.mysql.cj.jdbc.Driver
    redis:
      enable: true
      host: localhost
      port: 6379
      password: simple@123
  sql-index-package: com.edl.high.elf.repository.index  # index保存sql游标目录
  cache-table: edl_user|edl_role|edl_seller|edl_goods_category|edl_goods| # 需要缓存的表
  generate: # 代码生成配置
    spring: true  # 是否支持spring
    edl:    # 业务块标识
      schema: edl_stock # 数据库名
      package: com.edl.high.elf.repository.entity # entity目录
      tables: "edl_file_manage|edl_goods" # 需生成entity的表
      
~~~

### 三.代码生成器-GenerateStart

执行代码生成器(com.simple.repository.GenerateStart)生成entity、sqlIndex、repository

### 四.启动simple-repository
~~~

SimpleStart.run(new String[]{"--simple.env=dev"});
    
~~~

### 五.执行数据库操作

~~~

SimpleStart.run(new String[]{"--simple.env=dev"});
TestEntity entity = new TestEntity();
entity.age = 18;
entity.name = "张三";
new TestRepository().add(entity);

~~~

### 六.SQL索引操作

#### 1.创建.sql文件
./resources/test.sql
~~~
/**
* 查询测试
* @name testSelect
*/
select * from test where name like &{name} and age > ${age};
~~~

#### 2.执行代码生成器-GenerateStart生成SqlIndex
生成的代码如下
~~~

public class TestIndex {

   public static final String TEST_SELECT = "Test.testSelect";
 
}

~~~

#### 3.执行sql语句
~~~

SimpleStart.run(new String[]{"--simple.env=dev"});
TestSearch search = new TestSearch();
search.name = "%张三%";
search.age = 15;
testRepository.select(TestIndex.TEST_SELECT,search,TestEntity.class);

~~~
