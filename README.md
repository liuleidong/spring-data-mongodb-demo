---
layout: post
title: SpringMVC+Spring-Data+MongoDB4.0开启事务支持示例
tags: Spring5 Spring-Data MongoDB4.0 事务
categories: 技术原创
description: 探索使用Spring5和MongoDB4.0，如何开启事务
---

### 背景

最近项目中用到了Nosql数据库mongodb和SpringMVC框架。随着项目的深入，遇到了一些困难，于是把这些困难的解决记录下来，希望以后能帮助到其他人。

#### 技术介绍

##### Spring Data MongoDB

>  Spring Data’s mission is to provide a familiar and consistent, Spring-based programming model for data access while still retaining the special traits of the underlying data store.

Spring Data提供了一套基于Spring的用于数据访问的编程模型。包含了许多模块，我们主要使用的是[Spring Data  MongoDB](<https://spring.io/projects/spring-data-mongodb>)模块。

>  Spring Data for [MongoDB](https://www.mongodb.org/) is part of the umbrella Spring Data project which aims to provide a familiar and consistent Spring-based programming model for new datastores while retaining store-specific features and capabilities.

Spring Data MongoDB是Spring Data Project的一个模块，提供了一套类似于关系型数据库的接口，并且提供了基于注解的文档存储模式。

##### MongoDB

> MongoDB is a document database with the scalability and flexibility that you want with the querying and indexing that you need

MongoDB 是文档型数据库即所谓的Nosql数据库，存储的数据并不是关系型数据库中有规律的数据，而是使用json格式的文档。

![文档](https://github.com/liuleidong/MarkdownImg/blob/master/Spring-Data-MongoDB/Document.png?raw=true)

使用这种存储格式，MongoDB提供了良好的灵活性和可扩展性。

#### MongoDB数据库模型

- Embedded Data(嵌入型)

![embedded](https://github.com/liuleidong/MarkdownImg/blob/master/Spring-Data-MongoDB/embedded.png?raw=true)

嵌入型的数据模型即将所有的信息通过json嵌入文档的方式保存在一个大的文档中，一个文档即保存了所有的信息.这种模型：

1. 一个文档即包含了多个实体之间的关系
2. 由于MongoDB提供的事务是针对单文档的，所以这种存储模型不必担心事务问题
3. MongoDB针对单个文档的大小有限制，如果数据量太大可能会遇到这个问题

- References(引用型)

![references](https://github.com/liuleidong/MarkdownImg/blob/master/Spring-Data-MongoDB/reference.png?raw=true)

引用型通过将信息记录在不同的文档，并使用_id字段进行引用。这种模型：

1. 可以表示更加复杂的多对多的关系
2. 可以构建一个大型的分层数据集
3. 使用这个模型要考虑事务问题即是否需要保证上图中三个文档要么同时保存成功要么同时失败，不能出现单个成功其他失败现象。

#### 引用模型引发的事务支持

我们的项目中，最初使用的是嵌入型文档结构，一段时间后发现这种结构并不满足我们的需求，所以需要将结构改为引用型。由此引发了一系列的问题。

#### 版本问题

MongoDB Java Driver分为2.X和3.X。

- 2.X版本

MongoDB Java Driver2.X版本，对应的Spring框架版本是4.X，使用的Spring-Data-MongoDB是1.X的版本

```xml
        <!--MongoBD-Spring整合-->
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongo-java-driver</artifactId>
            <version>2.14.3</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-mongodb</artifactId>
            <version>1.10.7.RELEASE</version>
        </dependency>
```



- 3.X版本

MongoDB Java Driver3.X版本，对应的Spring框架版本是5.X版本，使用的Spring-Data-MongoDB是2.X的版本

```xml
        <!--MongoDB驱动包-->
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongo-java-driver</artifactId>
            <version>3.10.1</version>
        </dependency>
        <!--MongoDB核心包-->
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-mongodb</artifactId>
            <version>2.1.6.RELEASE</version>
        </dependency>
```

由于数据库模型改为了引用型，所以在添加数据的时候必须能够实现事务即如果失败则全部失败。为了实现这个特性遇到了以下几个坑：

- MongoDB在4.0之前只支持单文档事务，想要使用(Multi-Document Transaction)多文档事务，需要升级到4.0。
- MongoDB的StandAlone模式不支持事务，只有ReplicaSet(复制集)才支持事务
- MongoDB使用事务之后，数据库和集合必须提前创建
- MongoDB升级到4.0之后，MongoDB Java Driver、Spring Data MongoDB和Spring都要升级。尤其是Spring需要升级到5以上，项目中涉及到很多模块，其中在父模块指定引用了Spring4，如果全部升级可能会造成其他模块问题。最终使用maven的exclusions标签，单独给操作MongoDB的模块进行了升级。语法如下：

```xml
			<exclusions>
				<exclusion>
					<artifactId>spring-web</artifactId>
					<groupId>org.springframework</groupId>
				</exclusion>
				...
			</exclusions>
```

### MongoDB Windows 搭建副本集并开启访问控制环境搭建

[Deploy New Replica Set With Keyfile Access Control](<https://docs.mongodb.com/manual/tutorial/deploy-replica-set-with-keyfile-access-control/>)

#### 安装MongoDB

[下载MongoDB最新版](<https://www.mongodb.com/download-center/community?jmp=docs>)，双击安装：

![安装选项](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/MongoDB%E5%AE%89%E8%A3%85.jpg?raw=true)

Install Service as Network Service user选项，可选可不选，因为本次测试是在一台机器上启动多个mongod service来做副本集，启动的时候需要指定配置文件，如果这里安装了，就只能修改默认的配置文件了。

![Install Compass](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/Install%20Compass.jpg?raw=true)

Install MongoDB Compass选项要去掉，否则会下载，导致很慢

#### 设置环境变量

将mongodb安装后的bin目录加入到path中。

#### 生成密钥文件

密钥文件用于内部认证，只有使用了这个密钥的mongod才能加入到设置的副本集中，这里使用openssl生成密钥

```
openssl rand -base64 756 > <path-to-keyfile>
```

#### 修改配置文件

mongodb推荐的配置是三个节点，一个主节点两个副节点，所以这里需要准备三份配置文件。三份配置文件类似，只是数据目录和日志目录不同，一份配置文件内容如下：

```
# mongod.conf

# for documentation of all options, see:
#   http://docs.mongodb.org/manual/reference/configuration-options/

# Where and how to store data.
storage:
  dbPath: D:\hpep4\MongoDB\primary\data	#这里修改数据存储目录，三个节点分别配置
  journal:
    enabled: true
#  engine:
#  mmapv1:
#  wiredTiger:

# where to write logging data.
systemLog:
  destination: file
  logAppend: true
  path:  D:\hpep4\MongoDB\primary\log\mongod.log #这里修改日志存储目录，三个节点分别配置

# network interfaces
net:
  port: 27017		#端口修改，如果在同一台机器上部署三个节点，需要修改端口
  bindIp: 0.0.0.0	#绑定ip，默认只监听本机，如果需要提供外部连接需要改为0.0.0.0


#processManagement:

security:
  keyFile: D:\hpep4\MongoDB\mongokey.dat	#指定上一步生成的密钥文件路径
#  authorization: enabled

#operationProfiling:

replication:
#  oplogSizeMB: <int> 
  replSetName: rs0					#指定副本集名称
#  secondaryIndexPrefetch: <string>
#  enableMajorityReadConcern: <boolean>
#sharding:

## Enterprise-Only Options:

#auditLog:

#snmp:

```

#### 安装启动服务

```
mongod --config D:\hpep4\MongoDB\mongod0.cfg --serviceName MongoDB1 --serviceDisplayName MongoDB1 --install
mongod --config D:\hpep4\MongoDB\mongod1.cfg --serviceName MongoDB2 --serviceDisplayName MongoDB2 --install
mongod --config D:\hpep4\MongoDB\mongod2.cfg --serviceName MongoDB3 --serviceDisplayName MongoDB3 --install
net start MongoDB1
net start MongoDB2
net start MongoDB3
```

![安装服务](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/%E5%AE%89%E8%A3%85%E6%9C%8D%E5%8A%A1.jpg?raw=true)

#### 设置副本集

在终端中打开Mongo并连接，

![mongo](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/mongo.jpg?raw=true)

设置副本集成员

```javascript
config = 
    { 
     _id: "rs0", 
     members:[
         { _id : 0, host : "192.168.1.115:27017"},
         { _id : 1, host : "192.168.1.115:27018"},
         { _id : 2, host : "192.168.1.115:27019"}] 
	}
	rs.initiate(config)
```

![config](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/rsconfig.jpg?raw=true)

```
rs.status()查看状态
```

![status](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/rsstatus.jpg?raw=true)

#### 添加用户

搭建完成后，是连接不上数据库的，因为还没有添加用户。

![err](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/connect%20error.jpg?raw=true)

此时只能使用mongo shell本地连接情况下，添加一个用户

```javascript
admin = db.getSiblingDB("admin")
admin.createUser(
  {
    user: "fred",
    pwd: "changeme1",
    roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]
  }
)
```

![add user](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/add%20user.jpg?raw=true)

此时使用GUI管理工具，就可以连接到数据库了，不过需要注意的是要设置刚才添加的用户名密码

![auth](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/auth.jpg?raw=true)

继续使用mongo shell也可以，但是需要使用刚刚添加的用户名密码进行认证

```
db.getSiblingDB("admin").auth("fred", "changeme1" )
```

后续可以继续添加用户，比如给业务使用的数据库添加单独用户

![add user](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/add%20user1.jpg?raw=true)

至此mongodb副本集并开启认证环境搭建完毕。

### Demo代码

#### 代码结构

![代码结构](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/structure.jpg?raw=true)

#### MongoDB配置

```
	<!--3.服务器连接信息-->
	<bean id="mongoClientUri" class="com.mongodb.MongoClientURI">
		<constructor-arg name="uri" value="${mongo.uri}" />
	</bean>
	<bean id="mongoClient" class="com.mongodb.MongoClient">
		<constructor-arg ref="mongoClientUri" />
	</bean>
	<bean id="mongoDbFactory"
		class="org.springframework.data.mongodb.core.SimpleMongoDbFactory">
		<constructor-arg ref="mongoClient" />
		<constructor-arg name="databaseName" value="${mongo.dbname}" />
	</bean>
	<!--4.创建mongoTemplate模板-->
	<bean id="mongoTemplate" class="org.springframework.data.mongodb.core.MongoTemplate">
		<constructor-arg ref="mongoDbFactory" />
	</bean>
	<!--5.开启事务-->
	<bean id="mongoTransactionManager" class="org.springframework.data.mongodb.MongoTransactionManager">
        <constructor-arg name="dbFactory" ref="mongoDbFactory"/>
    </bean>
    <tx:annotation-driven transaction-manager="mongoTransactionManager" proxy-target-class="true"/>
```

如果需要使用事务，则必须在上述配置信息中加入5.开启事务代码

#### 添加用户流程

```mermaid
graph LR
A[UserController] --> B[IUserDao]
B[IUserDao] --> C[MongoTemplate]
```

其中Dao层的添加用户，如果不开启事务，代码如下

![不开启事务](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/addUser.jpg?raw=true)

开启事务如下：

![开启事务](https://github.com/liuleidong/MarkdownImg/blob/master/MongoDB/transaction.jpg?raw=true)

另外需要注意的一点是，开启事务之后，MongoDB的数据库和集合就不能自动创建了，需要提前创建好。在参考链接中提供了扫描Bean来创建的办法。

最后Demo源码地址

### 参考链接

[SpringDataMongoDB多文档事务](<https://www.jianshu.com/p/2faf0e6b6627>)

[SpringDataMongoDB自动创建集合](<https://www.jianshu.com/p/1dcf39521df0>)

[mongodb副本集和开启认证](<https://docs.mongodb.com/manual/tutorial/deploy-replica-set-with-keyfile-access-control/>)

[Spring Data MongoDB - Reference Documentation](<https://docs.spring.io/spring-data/mongodb/docs/2.1.6.RELEASE/reference/html/>)

[Deploy New Replica Set With Keyfile Access Control](<https://docs.mongodb.com/manual/tutorial/deploy-replica-set-with-keyfile-access-control/>)

[MongoDB Documentation](<https://docs.mongodb.com/manual/introduction/>)

