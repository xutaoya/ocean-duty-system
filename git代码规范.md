## <font style="color:rgb(31, 35, 40);">分支命名</font>
### <font style="color:rgb(31, 35, 40);">master 分支</font>
<font style="color:rgb(31, 35, 40);">master 为主分支，也是用于部署生产环境的分支，需要确保master分支稳定性。master 分支一般由 release 以及 hotfix 分支合并，任何时间都不能直接修改代码。</font>

### <font style="color:rgb(31, 35, 40);">develop 分支</font>
<font style="color:rgb(31, 35, 40);">develop 为开发环境分支，始终保持最新完成以及bug修复后的代码，用于前后端联调。一般开发的新功能时，feature分支都是基于develop分支创建的。</font>

### <font style="color:rgb(31, 35, 40);">feature 分支</font>
<font style="color:rgb(31, 35, 40);">开发新功能时，以develop为基础创建feature分支。</font>

<font style="color:rgb(31, 35, 40);">分支命名时以</font><font style="color:rgb(31, 35, 40);"> </font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">feature/</font>`<font style="color:rgb(31, 35, 40);"> </font><font style="color:rgb(31, 35, 40);">开头，后面可以加上开发的功能模块， 命名示例：</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">feature/user_module</font>`<font style="color:rgb(31, 35, 40);">、</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">feature/cart_module</font>`

### <font style="color:rgb(31, 35, 40);">test分支</font>
<font style="color:rgb(31, 35, 40);">test为测试环境分支，外部用户无法访问，专门给测试人员使用，版本相对稳定。</font>

### <font style="color:rgb(31, 35, 40);">release分支</font>
<font style="color:rgb(31, 35, 40);">release 为预上线分支（预发布分支），UAT测试阶段使用。一般由 test 或 hotfix 分支合并，不建议直接在 release 分支上直接修改代码。</font>

### <font style="color:rgb(31, 35, 40);">hotfix 分支</font>
<font style="color:rgb(31, 35, 40);">线上出现紧急问题时，需要及时修复，以master分支为基线，创建hotfix分支。修复完成后，需要合并到 master 分支和 develop 分支。</font>

<font style="color:rgb(31, 35, 40);">分支命名以</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">hotfix/</font>`<font style="color:rgb(31, 35, 40);"> </font><font style="color:rgb(31, 35, 40);">开头的为修复分支，它的命名规则与 feature 分支类似。</font>

## <font style="color:rgb(31, 35, 40);">分支与环境对应关系</font>
<font style="color:rgb(31, 35, 40);">在系统开发过程中常用的环境：</font>

+ <font style="color:rgb(31, 35, 40);">DEV 环境（Development environment）：用于开发者调试使用</font>
+ <font style="color:rgb(31, 35, 40);">FAT环境（Feature Acceptance Test environment）：功能验收测试环境，用于测试环境下的软件测试者测试使用</font>
+ <font style="color:rgb(31, 35, 40);">UAT环境 （User Acceptance Test environment）：用户验收测试环境，用于生产环境下的软件测试者测试使用</font>
+ <font style="color:rgb(31, 35, 40);">PRO 环境（Production environment）：生产环境</font>

<font style="color:rgb(31, 35, 40);">对应关系：</font>

| **<font style="color:rgb(31, 35, 40);">分支</font>** | **<font style="color:rgb(31, 35, 40);">功能</font>** | **<font style="color:rgb(31, 35, 40);">环境</font>** | **<font style="color:rgb(31, 35, 40);">可访问</font>** |
| --- | --- | --- | --- |
| <font style="color:rgb(31, 35, 40);">master</font> | <font style="color:rgb(31, 35, 40);">主分支，稳定版本</font> | <font style="color:rgb(31, 35, 40);">PRO</font> | <font style="color:rgb(31, 35, 40);">是</font> |
| <font style="color:rgb(31, 35, 40);">develop</font> | <font style="color:rgb(31, 35, 40);">开发分支，最新版本</font> | <font style="color:rgb(31, 35, 40);">DEV</font> | <font style="color:rgb(31, 35, 40);">是</font> |
| <font style="color:rgb(31, 35, 40);">feature</font> | <font style="color:rgb(31, 35, 40);">开发分支，实现新特性</font> | | <font style="color:rgb(31, 35, 40);">否</font> |
| <font style="color:rgb(31, 35, 40);">test</font> | <font style="color:rgb(31, 35, 40);">测试分支，功能测试</font> | <font style="color:rgb(31, 35, 40);">FAT</font> | <font style="color:rgb(31, 35, 40);">是</font> |
| <font style="color:rgb(31, 35, 40);">release</font> | <font style="color:rgb(31, 35, 40);">预上线分支，发布新版本</font> | <font style="color:rgb(31, 35, 40);">UAT</font> | <font style="color:rgb(31, 35, 40);">是</font> |
| <font style="color:rgb(31, 35, 40);">hotfix</font> | <font style="color:rgb(31, 35, 40);">紧急修复分支，修复线上bug</font> | | <font style="color:rgb(31, 35, 40);">否</font> |


### <font style="color:rgb(31, 35, 40);">分支合并流程规范</font>
<font style="color:rgb(31, 35, 40);">业界常见的两大主分支（master、develop）、三个辅助分支（feature、release、hotfix）的生命周期：</font>

<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/58483328/1777182331189-d1a659db-0b38-4f47-a03c-e3ba1535e04c.png)

<font style="color:rgb(31, 35, 40);">以上生命周期仅作参考，不同开发团队可能有不同的规范，可自行灵活定义。</font>

<font style="color:rgb(31, 35, 40);">例如我们团队在开发时，至少需要保证以下流程：</font>

+ <font style="color:rgb(31, 35, 40);">develop 分支和 hotfix 分支，必须从 master 分支检出</font>
+ <font style="color:rgb(31, 35, 40);">由 develop 分支合并到 test 分支</font>
+ <font style="color:rgb(31, 35, 40);">功能测试无误后，由 test 分支合并到 release 分支</font>
+ <font style="color:rgb(31, 35, 40);">UAT测试通过后，由 release 分支合并到 master分支</font>
+ <font style="color:rgb(31, 35, 40);">对于工作量小的功能开发（工时小于1天），可以直接在devolop 分支进行开发，否则由 develop 分支检出 feature 分支进行开发，开发完后合并到develop 分支</font>

## <font style="color:rgb(31, 35, 40);">Git Commit Message规范</font>
<font style="color:rgb(31, 35, 40);">Git commit message规范指提交代码时编写的规范注释，编写良好的Commit messages可以达到3个重要的目的：</font>

+ <font style="color:rgb(31, 35, 40);">加快代码review的流程</font>
+ <font style="color:rgb(31, 35, 40);">帮助我们编写良好的版本发布日志</font>
+ <font style="color:rgb(31, 35, 40);">让之后的维护者了解代码里出现特定变化和feature被添加的原因</font>

### <font style="color:rgb(31, 35, 40);">Angular Git Commit Guidelines</font>
<font style="color:rgb(31, 35, 40);">业界应用的比较广泛的是Angular Git Commit Guidelines：</font>

```plain
<type>(<scope>): <subject>  
<BLANK LINE>  
<body>  
<BLANK LINE>  
<footer>
```

+ <font style="color:rgb(31, 35, 40);">type：提交类型</font>
+ <font style="color:rgb(31, 35, 40);">scope：可选项，本次 commit 波及的范围</font>
+ <font style="color:rgb(31, 35, 40);">subject：简明扼要的阐述下本次 commit 的主旨，在</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">Angular Git Commit Guidelines</font>`<font style="color:rgb(31, 35, 40);">中强调了三点。使用祈使句，首字母不要大写，结尾无需添加标点</font>
+ <font style="color:rgb(31, 35, 40);">body: 同样使用祈使句，在主体内容中我们需要把本次 commit 详细的描述一下，比如此次变更的动机</font>
+ <font style="color:rgb(31, 35, 40);">footer: 描述下与之关联的 issue 或 break change</font>

### <font style="color:rgb(31, 35, 40);">简易版</font>
<font style="color:rgb(31, 35, 40);">项目中实际可以采用简易版规范：</font>

```plain
<type>(<scope>):<subject>
```

### <font style="color:rgb(31, 35, 40);">type规范</font>
`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">Angular Git Commit Guidelines</font>`<font style="color:rgb(31, 35, 40);">中推荐的type类型如下：</font>

+ <font style="color:rgb(31, 35, 40);">feat: 新增功能</font>
+ <font style="color:rgb(31, 35, 40);">fix: 修复bug</font>
+ <font style="color:rgb(31, 35, 40);">docs: 仅文档更改</font>
+ <font style="color:rgb(31, 35, 40);">style: 不影响代码含义的更改（空白、格式设置、缺失 分号等）</font>
+ <font style="color:rgb(31, 35, 40);">refactor: 既不修复bug也不添加特性的代码更改</font>
+ <font style="color:rgb(31, 35, 40);">perf: 改进性能的代码更改</font>
+ <font style="color:rgb(31, 35, 40);">test: 添加缺少的测试或更正现有测试</font>
+ <font style="color:rgb(31, 35, 40);">chore: 对构建过程或辅助工具和库（如文档）的更改</font>

<font style="color:rgb(31, 35, 40);">除此之外，还有一些常用的类型：</font>

+ <font style="color:rgb(31, 35, 40);">delete：删除功能或文件</font>
+ <font style="color:rgb(31, 35, 40);">modify：修改功能</font>
+ <font style="color:rgb(31, 35, 40);">build：改变构建流程，新增依赖库、工具等（例如webpack、gulp、npm修改）</font>
+ <font style="color:rgb(31, 35, 40);">test：测试用例的新增、修改</font>
+ <font style="color:rgb(31, 35, 40);">ci：自动化流程配置修改</font>
+ <font style="color:rgb(31, 35, 40);">revert：回滚到上一个版本</font>

### <font style="color:rgb(31, 35, 40);">单次提交注意事项</font>
+ <font style="color:rgb(31, 35, 40);">提交问题必须为同一类别</font>
+ <font style="color:rgb(31, 35, 40);">提交问题不要超过3个</font>
+ <font style="color:rgb(31, 35, 40);">提交的commit发现不符合规范，</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">git commit --amend -m "新的提交信息"</font>`<font style="color:rgb(31, 35, 40);">或</font><font style="color:rgb(31, 35, 40);"> </font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">git reset --hard HEAD</font>`<font style="color:rgb(31, 35, 40);"> </font><font style="color:rgb(31, 35, 40);">重新提交一次</font>

## <font style="color:rgb(31, 35, 40);">配置.gitignore文件</font>
`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">.gitignore</font>`<font style="color:rgb(31, 35, 40);">是一份用于忽略不必提交的文件的列表，项目中可以根据实际需求统一</font>`<font style="color:rgb(31, 35, 40);background-color:rgba(175, 184, 193, 0.2);">.gitignore</font>`<font style="color:rgb(31, 35, 40);">文件，减少不必要的文件提交和冲突，净化代码库环境。</font>

<font style="color:rgb(31, 35, 40);">通用文件示例：</font>

```bash
HELP.md  
target/  
!.mvn/wrapper/maven-wrapper.jar  
!**/src/main/**/target/  
!**/src/test/**/target/  
  
### STS ###  
.apt_generated  
.classpath  
.factorypath  
.project  
.settings  
.springBeans  
.sts4-cache  
  
### IntelliJ IDEA ###  
.idea  
*.iws  
*.iml  
*.ipr  
  
### NetBeans ###  
/nbproject/private/  
/nbbuild/  
/dist/  
/nbdist/  
/.nb-gradle/  
build/  
!**/src/main/**/build/  
!**/src/test/**/build/  
  
### VS Code ###  
.vscode/  
  
# Log file  
*.log  
/logs*  
  
# BlueJ files  
*.ctxt  
  
# Mobile Tools for Java (J2ME)  
.mtj.tmp/  
  
# Package Files #  
*.jar  
*.war  
*.ear  
*.zip  
*.tar.gz  
*.rar  
*.cmd
```

## <font style="color:rgb(31, 35, 40);">其他</font>
<font style="color:rgb(31, 35, 40);">此外，还有一些其他建议：</font>

+ <font style="color:rgb(31, 35, 40);">master 分支的每一次更新，都建议打 tag 添加标签，通常为对应版本号，便于管理</font>
+ <font style="color:rgb(31, 35, 40);">feature分支、hotfix分支在合并后可以删除，避免分支过多管理混乱</font>
+ <font style="color:rgb(31, 35, 40);">每次 pull 代码前，提交本地代码到本地库中，否则可能回出现合并代码出错，导致代码丢失</font>

  


