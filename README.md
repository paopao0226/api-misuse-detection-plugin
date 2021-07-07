# API_Misuse_Detection_plugin

#### 介绍
使用约束知识图谱进行API误用检测

#### 软件架构
软件架构说明


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)


#### 编写说明

关于项目的结构

1.  domain:
    domain中是实体层APIClass和APIMethod,不要随意更改，如果必须要添加新的属性，请同时添加新的构造方法，
不要修改原有构造方法。

2.  caveat:
    这里存放的是关于APIMethod的警告，在实现新的类型的API约束时，在这里新建一个这个约束类型的caveat,并
实现Caveat接口，caveat中应该至少有一个checkViolation（）方法用于判断是否违反约束，和一个相应的rule用于
存放约束规则。

3.  rule：
    rule是用于存储约束规则的，每一种类型的约束应该有一种相应的rule，如果两个约束类型的json结构一样，可以考虑
用同一个rule。

4. collector
    这里存放的是用于从json中读取约束的类，每一种类型的约束应当有一个相应的collector,这个类中应至少有一个loadRule（）
方法，写法参考CallOrderCollection

5. inspection
   这里存放的inspection，是用于审查代码，提取语句，然后判断是否违反约束，每一种类型的约束应该有一个相应的inspection，
写完inspection后，要在plugin.xml中注册后方能生效