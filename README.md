# BEST API

> 一个丰富的API开放调用平台，为开发者提供便捷、实用的API调用体验
>
> Java + React 全栈项目，包括网站前台+管理员后台，感谢[鱼皮](https://github.com/liyupi)提供的后端开发基础框架，我也是在此基础上作拓展
>

## 项目介绍

BEST API 平台初衷是尽可能地帮助和服务更多的用户和开发者， 为开发者提供API接口，提高开发者开发效率的平台。我们可以为用户提供各种类型的接口服务，使他们能够更高效地完成需求，例如：获取今日天气、获取金句、随机头像等服务。

项目后端使用语言为Java，包括现在市面上主流技术栈，采用微服务架构开发，解耦业务模块，前端使用React，Ant Design Pro + Ant Design组件库，使用现成组件库快速开发项目。

## 技术栈

### 前端技术栈

- 开发框架：React、Umi
- 脚手架：Ant Design Pro
- 组件库：Ant Design、Ant Design Components
- 语法扩展：TypeScript、Less
- 打包工具：Webpack
- 代码规范：ESLint、StyleLint、Prettier

### 后端技术栈

* 主语言：Java
* 框架：SpringBoot 、Mybatis、Spring Cloud
* 数据库：Mysql8.0、Redis
* 中间件：RabbitMq
* 注册中心：Nacos
* 服务调用：Dubbo
* 网关：Spring Cloud  Gateway

## 快速上手

### 后端

服务启动顺序参考：

1. best-api-admin
2. best-pi-interface
3. best-api-gateway

## 功能模块

> 用户、管理员

* * 登录注册
  * 个人主页
  * 设置个人信息
  * 管理员：用户管理
  * 管理员：接口管理
  * 管理员：接口分析
* 接口
  * 浏览接口信息
  * 在线调用接口
  * 接口搜索
  * 用户上传自己的接口

### 后端模块

* best-api-admin：后端服务，提供用户、接口等基本操作
* best-api-common：项目公共模块，包含一些公用的实体类，远程调用接口
* best-api-gateway：api网关，整个后端的入口，作服务转发、用户鉴权、统一日志、服务接口调用计数
* best-api-interface：平台提供的接口服务，以及通用调用其他外部接口的方法，目前平台自己只有简单的几个接口，可以自行拓展

## 系统架构

> 仅供参考

![系统架构](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84.png)

## 项目展示

* 登陆注册

![登录](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E7%99%BB%E5%BD%95.png)

![注册](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%B3%A8%E5%86%8C.png)

* API商店

![api商店](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/api%E5%95%86%E5%BA%97.png)

- 我开通的接口

  ![我开通的接口](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%88%91%E5%BC%80%E9%80%9A%E7%9A%84%E6%8E%A5%E5%8F%A3.png)

- 我创建的接口

  ![我创建的接口](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%88%91%E5%88%9B%E5%BB%BA%E7%9A%84%E6%8E%A5%E5%8F%A3.png)

* 接口详情以及在线调用

![接口详情以及在线调用](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%8E%A5%E5%8F%A3%E5%9C%A8%E7%BA%BF%E8%B0%83%E7%94%A8-1.png)



![接口详情以及在线调用](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%8E%A5%E5%8F%A3%E5%9C%A8%E7%BA%BF%E8%B0%83%E7%94%A8-2.png)

* 用户、接口管理页

![用户管理页](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E7%94%A8%E6%88%B7%E7%AE%A1%E7%90%86.png)

![接口管理](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%8E%A5%E5%8F%A3%E7%AE%A1%E7%90%86.png)

* 分析页

![分析页](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E6%8E%A5%E5%8F%A3%E8%B0%83%E7%94%A8%E6%AC%A1%E6%95%B0%E7%BB%9F%E8%AE%A1%E5%88%86%E6%9E%90.png)

* 个人中心

![个人中心](https://nero-api-imagehost.oss-cn-hangzhou.aliyuncs.com/images/%E4%B8%AA%E4%BA%BA%E4%B8%AD%E5%BF%83.png)
