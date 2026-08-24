### <font style="color:rgb(15, 17, 21);">(一) Vue 编码基础</font>
<font style="color:rgb(15, 17, 21);">vue 项目规范以 Vue 官方规范（</font>[<font style="color:rgb(57, 100, 254);">https://cn.vuejs.org/v2/style-guide/</font>](https://cn.vuejs.org/v2/style-guide/)<font style="color:rgb(15, 17, 21);">）中的 A 规范为基础，在其上面进行项目开发，故所有代码均遵守该规范。</font>

<font style="color:rgb(15, 17, 21);">请仔仔细细阅读 Vue 官方规范，切记，此为第一步。</font>

#### <font style="color:rgb(15, 17, 21);">组件规范</font>
1. <font style="color:rgb(15, 17, 21);">组件名为多个单词。</font>

<font style="color:rgb(15, 17, 21);">组件名应该始终是多个单词组成（大于等于 2），且命名规范为</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">KebabCase</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">格式。</font><font style="color:rgb(15, 17, 21);">  
</font><font style="color:rgb(15, 17, 21);">这样做可以避免跟现有的以及未来的 HTML 元素相冲突，因为所有的 HTML 元素名称都是单个单词的。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
export default {
  name: 'TodoItem'
  // ...
};
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
export default {
  name: 'Todo',
  // ...
}
export default {
  name: 'todo-item',
  // ...
}
```

1. <font style="color:rgb(15, 17, 21);">组件文件名为 pascal-case 格式</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- my-component.vue
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- myComponent.vue
|- MyComponent.vue
```

1. <font style="color:rgb(15, 17, 21);">基础组件文件名为 base 开头，使用完整单词而不是缩写。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- base-button.vue
|- base-table.vue
|- base-icon.vue
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- MyButton.vue
|- VueTable.vue
|- Icon.vue
```

1. <font style="color:rgb(15, 17, 21);">和父组件紧密耦合的子组件应该以父组件名作为前缀命名</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- todo-list.vue
|- todo-list-item.vue
|- todo-list-item-button.vue
|- user-profile-options.vue（完整单词）
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components/
|- TodoList.vue
|- TodoItem.vue
|- TodoButton.vue
|- UProfOpts.vue（使用了缩写）
```

1. <font style="color:rgb(15, 17, 21);">在 Template 模板中使用组件，应使用 PascalCase 模式，并且使用自闭合组件。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<!-- 在单文件组件、字符串模板和 JSX 中 -->
<MyComponent />
<Row><table :column="data"/></Row>
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

<font style="color:rgb(15, 17, 21);"><my-component /> <row><table :column="data"/></row></font>

1. <font style="color:rgb(15, 17, 21);">组件的 data 必须是一个函数</font>

<font style="color:rgb(15, 17, 21);">当在组件中使用 data 属性的时候（除了 new Vue 外的任何地方），它的值必须是返回一个对象的函数。因为如果直接是一个对象的话，子组件之间的属性值会互相影响。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
export default {
  data () {
    return {
      name: 'jack'
    }
  }
}
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
export default {
  data: {
    name: 'jack'
  }
}
```

1. <font style="color:rgb(15, 17, 21);">Prop 定义应该尽量详细</font>
+ <font style="color:rgb(15, 17, 21);">必须使用 camelCase 驼峰命名</font>
+ <font style="color:rgb(15, 17, 21);">必须指定类型</font>
+ <font style="color:rgb(15, 17, 21);">必须加上注释，表明其含义</font>
+ <font style="color:rgb(15, 17, 21);">必须加上 required 或者 default，两者二选其一</font>
+ <font style="color:rgb(15, 17, 21);">如果有业务需要，必须加上 validator 验证</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
props: {
  // 组件状态，用于控制组件的颜色
  status: {
    type: String,
    required: true,
    validator: function (value) {
      return ['succ', 'info', 'error'].indexOf(value) !== -1
    }
  },
  // 用户级别，用于显示皇冠个数
  userLevel: {
    type: String,
    required: true
  }
}
```

1. <font style="color:rgb(15, 17, 21);">为组件样式设置作用域</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>
  <button class="btn btn-close">X</button>
</template>

<!-- 使用 `scoped` 特性 -->
<style scoped>
  .btn-close {
    background-color: red;
  }
</style>
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>
  <button class="btn btn-close">X</button>
</template>
<!-- 没有使用 `scoped` 特性 -->
<style>
  .btn-close {
    background-color: red;
  }
</style>
```

1. <font style="color:rgb(15, 17, 21);">如果特性元素较多，应该主动换行。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<MyComponent foo="a" bar="b" baz="c"
             foo="a" bar="b" baz="c"
             foo="a" bar="b" baz="c"
/>
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

<font style="color:rgb(15, 17, 21);"><MyComponent foo="a" bar="b" baz="c" foo="a" bar="b" baz="c" foo="a" bar="b" baz="c" foo="a" bar="b" baz="c"/></font>

#### <font style="color:rgb(15, 17, 21);">模板中使用简单的表达式</font>
<font style="color:rgb(15, 17, 21);">组件模板应该只包含简单的表达式，复杂的表达式则应该重构为计算属性或方法。复杂表达式会让你的模板变得不那么声明式。我们应该尽量描述应该出现的是什么，而非如何计算那个值。而且计算属性和方法使得代码可以重用。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>
  <p>{{ normalizedFullName }}</p>
</template>

// 复杂表达式已经移入一个计算属性

computed: {
  normalizedFullName: function () {
    return this.fullName.split(' ').map(function (word) {
      return word[0].toUpperCase() + word.slice(1)
    }).join(' ')
  }
}
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>
  <p>
    {{
      fullName.split(' ').map(function (word) {
        return word[0].toUpperCase() + word.slice(1)
      }).join(' ')
    }}
  </p>
</template>
```

#### <font style="color:rgb(15, 17, 21);">指令都使用缩写形式</font>
<font style="color:rgb(15, 17, 21);">指令推荐都使用缩写形式，(用</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">:</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">表示</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">v-bind:</font>`<font style="color:rgb(15, 17, 21);">、用</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">@</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">表示</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">v-on:</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">和用</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">#</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">表示</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">v-slot:</font>`<font style="color:rgb(15, 17, 21);">)</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<input
  @input="onInput"
  @focus="onFocus"
>
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<input
  v-on:input="onInput"
  @focus="onFocus"
>
```

#### <font style="color:rgb(15, 17, 21);">标签顺序保持一致</font>
<font style="color:rgb(15, 17, 21);">单文件组件应该总是让标签顺序保持为</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);"><template></font>`<font style="color:rgb(15, 17, 21);">、</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);"><script></font>`<font style="color:rgb(15, 17, 21);">、</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);"><style></font>`<font style="color:rgb(15, 17, 21);">。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>...</template>
<script>...</script>
<style>...</style>
```

<font style="color:rgb(15, 17, 21);">反例：</font>

<font style="color:rgb(15, 17, 21);">html</font>

```plain
<template>...</template>
<style>...</style>
<script>...</script>
```

#### <font style="color:rgb(15, 17, 21);">必须为 v-for 设置键值 key</font>
#### <font style="color:rgb(15, 17, 21);">v-show 与 v-if 选择</font>
<font style="color:rgb(15, 17, 21);">如果运行时,需要非常频繁地切换,使用 v-show ;如果在运行时,条件很少改变,使用 v-if。</font>

#### <font style="color:rgb(15, 17, 21);">script 标签内部结构顺序</font>
`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">components</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">props</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">data</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">computed</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">watch</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">filter</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">> 钩子函数（钩子函数按其执行顺序） ></font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">methods</font>`

#### <font style="color:rgb(15, 17, 21);">Vue Router 规范</font>
1. <font style="color:rgb(15, 17, 21);">页面跳转数据传递使用路由参数</font>

<font style="color:rgb(15, 17, 21);">页面跳转,例如 A 页面跳转到 B 页面,需要将 A 页面的数据传递到 B 页面,推荐使用路由参数进行传参,而不是将需要传递的数据保存 vuex,然后在 B 页面取出 vuex 的数据,因为如果在 B 页面刷新会导致 vuex 数据丢失,导致 B 页面无法正常显示数据。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
let id = '123';
this.$router.push({ name: 'userCenter', query: { id: id } });
```

1. <font style="color:rgb(15, 17, 21);">使用路由懒加载（延迟加载）机制</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
{
  path: '/uploadAttachment',
  name: 'uploadAttachment',
  meta: {
    title: '上传附件'
  },
  component: () => import('@/view/components/uploadAttachment/index.vue')
}
```

1. <font style="color:rgb(15, 17, 21);">router 中的命名规范</font>

<font style="color:rgb(15, 17, 21);">path、childrenPoints 命名规范采用 kebab-case 命名规范（尽量 vue 文件的目录结构保持一致，因为目录、文件名都是 kebab-case，这样很方便找到对应的文件）</font>

<font style="color:rgb(15, 17, 21);">name 命名规范采用 PascalCase 命名规范且和 component 组件名保持一致！（因为要保持 keep-alive 特性，keep-alive 按照 component 的 name 进行缓存，所以两者必须高度保持一致）</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
// 动态加载
export const reload = [
  {
    path: '/reload',
    name: 'reload',
    component: Main,
    meta: {
      title: '动态加载',
      icon: 'icon iconfont'
    },
    children: [
      {
        path: '/reload/smart-reload-list',
        name: 'SmartReloadList',
        meta: {
          title: 'SmartReload',
          childrenPoints: [
            {
              title: '查询',
              name: 'smart-reload-search'
            },
            {
              title: '执行 reload',
              name: 'smart-reload-update'
            },
            {
              title: '查看执行结果',
              name: 'smart-reload-result'
            }
          ]
        },
        component: () =>
          import('@/views/reload/smart-reload/smart-reload-list.vue')
      }
    ]
  }
];
```

1. <font style="color:rgb(15, 17, 21);">router 中的 path 命名规范</font>

<font style="color:rgb(15, 17, 21);">path 除了采用 kebab-case 命名规范以外，必须以</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">开头，即使是 children 里的 path 也要以</font><font style="color:rgb(15, 17, 21);"> </font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/</font>`<font style="color:rgb(15, 17, 21);"> </font><font style="color:rgb(15, 17, 21);">开头。如下示例</font>

<font style="color:rgb(15, 17, 21);">目的：</font>

<font style="color:rgb(15, 17, 21);">经常有这样的场景：某个页面有问题，要立刻找到这个 vue 文件，如果不用以</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/</font>`<font style="color:rgb(15, 17, 21);">开头，path 为 parent 和 children 组成的，可能经常需要在 router 文件里搜索多次才能找到，而如果以</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/</font>`<font style="color:rgb(15, 17, 21);">开头，则能立刻搜索到对应的组件</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
{
  path: '/file',
  name: 'File',
  component: Main,
  meta: {
    title: '文件服务',
    icon: 'ios-cloud-upload'
  },
  children: [
    {
      path: '/file/file-list',
      name: 'FileList',
      component: () => import('@/views/file/file-list.vue')
    },
    {
      path: '/file/file-add',
      name: 'FileAdd',
      component: () => import('@/views/file/file-add.vue')
    },
    {
      path: '/file/file-update',
      name: 'FileUpdate',
      component: () => import('@/views/file/file-update.vue')
    }
  ]
}
```

---

### <font style="color:rgb(15, 17, 21);">(二) Vue 项目目录规范</font>
#### <font style="color:rgb(15, 17, 21);">基础</font>
<font style="color:rgb(15, 17, 21);">vue 项目中的所有命名一定要与后端命名统一。</font>

<font style="color:rgb(15, 17, 21);">比如权限：后端 privilege, 前端无论 router, store, api 等都必须使用 privilege 单词！</font>

#### <font style="color:rgb(15, 17, 21);">使用 Vue-cli 脚手架</font>
<font style="color:rgb(15, 17, 21);">使用 vue-cli3 来初始化项目，项目名按照上面的命名规范。</font>

#### <font style="color:rgb(15, 17, 21);">目录说明</font>
<font style="color:rgb(15, 17, 21);">目录名按照上面的命名规范，其中 components 组件用大写驼峰，其余除 components 组件目录外的所有目录均使用 kebab-case 命名。</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
src                            源码目录
|-- api                        所有 api 接口
|-- assets                     静态资源, images, icons, styles 等
|-- components                 公用组件
|-- config                     配置信息
|-- constants                  常量信息，项目所有 Enum, 全局常量等
|-- directives                 自定义指令
|-- filters                    过滤器，全局工具
|-- datas                      模拟数据，临时存放
|-- lib                        外部引用的插件存放及修改文件
|-- mock                       模拟接口，临时存放
|-- plugins                    插件，全局使用
|-- router                     路由，统一管理
|-- store                      vuex, 统一管理
|-- themes                     自定义样式主题
|-- views                      视图目录
|   |-- role                   role 模块名
|   |   |-- role-list.vue      role 列表页面
|   |   |-- role-add.vue       role 新建页面
|   |   |-- role-update.vue    role 更新页面
|   |   |-- index.less         role 模块样式
|   |   |-- components         role 模块通用组件文件夹
|   |-- employee               employee 模块
```

1. <font style="color:rgb(15, 17, 21);">api 目录</font>
+ <font style="color:rgb(15, 17, 21);">文件、变量命名要与后端保持一致。</font>
+ <font style="color:rgb(15, 17, 21);">此目录对应后端 API 接口，按照后端一个 controller 一个 api.js 文件。若项目较大时，可以按照业务划分子目录，并与后端保持一致。</font>
+ <font style="color:rgb(15, 17, 21);">api 中的方法名字要与后端 api.url 尽量保持语义高度一致性。</font>
+ <font style="color:rgb(15, 17, 21);">对于 api 中的每个方法要添加注释，注释与后端 swagger 文档保持一致。</font>

<font style="color:rgb(15, 17, 21);">正例：</font>

<font style="color:rgb(15, 17, 21);">后端 url: EmployeeController.java</font><font style="color:rgb(15, 17, 21);">  
</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/employee/add</font>`<font style="color:rgb(15, 17, 21);">  
</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/employee/delete/{id}</font>`<font style="color:rgb(15, 17, 21);">  
</font>`<font style="color:rgb(15, 17, 21);background-color:rgb(235, 238, 242);">/employee/update</font>`

<font style="color:rgb(15, 17, 21);">前端： employee.js</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
// 添加员工
addEmployee: (data) => {
  return postAxios('/employee/add', data)
},
// 更新员工信息
updateEmployee: (data) => {
  return postAxios('/employee/update', data)
},
// 删除员工
deleteEmployee: (employeeId) => {
  return postAxios('/employee/delete/' + employeeId)
}
```

1. <font style="color:rgb(15, 17, 21);">assets 目录</font>

<font style="color:rgb(15, 17, 21);">assets 为静态资源，里面存放 images, styles, icons 等静态资源，静态资源命名格式为 kebab-case</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
assets
|-- icons
|-- images
|   |-- background-color.png
|   |-- upload-header.png
|-- styles
```

1. <font style="color:rgb(15, 17, 21);">components 目录</font>

<font style="color:rgb(15, 17, 21);">此目录应按照组件进行目录划分，目录命名为 KebabCase，组件命名规则也为 KebabCase</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
components
|-- error-log
|   |-- index.vue
|   |-- index.less
|-- markdown-editor
|   |-- index.vue
|   |-- index.js
|-- kebab-case
```

1. <font style="color:rgb(15, 17, 21);">constants 目录</font>

<font style="color:rgb(15, 17, 21);">此目录存放项目所有常量，如果常量在 vue 中使用，请使用 vue-enum 插件(</font>[<font style="color:rgb(57, 100, 254);">https://www.npmjs.com/package/vue-enum</font>](https://www.npmjs.com/package/vue-enum)<font style="color:rgb(15, 17, 21);">)</font>

<font style="color:rgb(15, 17, 21);">目录结构：</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
constants
|-- index.js
|-- role.js
|-- employee.js
```

<font style="color:rgb(15, 17, 21);">例子：employee.js</font>

<font style="color:rgb(15, 17, 21);">javascript</font>

```plain
export const EMPLOYEE_STATUS = {
  NORMAL: {
    value: 1,
    desc: '正常'
  },
  DISABLED: {
    value: 1,
    desc: '禁用'
  },
  DELETED: {
    value: 2,
    desc: '已删除'
  }
};

export const EMPLOYEE_ACCOUNT_TYPE = {
  QQ: {
    value: 1,
    desc: 'QQ登录'
  },
  WECHAT: {
    value: 2,
    desc: '微信登录'
  },
  DINGDING: {
    value: 3,
    desc: '钉钉登录'
  },
  USERNAME: {
    value: 4,
    desc: '用户名密码登录'
  }
};

export default {
  EMPLOYEE_STATUS,
  EMPLOYEE_ACCOUNT_TYPE
};
```

1. <font style="color:rgb(15, 17, 21);">router 与 store 目录</font>

<font style="color:rgb(15, 17, 21);">这两个目录一定要将业务进行拆分，不能放到一个 js 文件里。</font>

<font style="color:rgb(15, 17, 21);">router 尽量按照 views 中的结构保持一致</font>

<font style="color:rgb(15, 17, 21);">store 按照业务进行拆分不同的 js 文件</font>

1. <font style="color:rgb(15, 17, 21);">views 目录</font>
+ <font style="color:rgb(15, 17, 21);">命名要与后端、router、api 等保持一致</font>
+ <font style="color:rgb(15, 17, 21);">components 中组件要使用 PascalCase 规则</font>

<font style="color:rgb(15, 17, 21);">text</font>

```plain
|-- views                          视图目录
|   |-- role                       role 模块名
|   |   |-- role-list.vue          role 列表页面
|   |   |-- role-add.vue           role 新建页面
|   |   |-- role-update.vue        role 更新页面
|   |   |-- index.less             role 模块样式
|   |   |-- components             role 模块通用组件文件夹
|   |   |   |-- role-header.vue    role 头部组件
|   |   |   |-- role-modal.vue     role 弹出框组件
|   |-- employee                   employee 模块
|   |-- behavior-log               行为日志 log 模块
|   |-- code-generator             代码生成器模块
```

#### <font style="color:rgb(15, 17, 21);">注释说明</font>
<font style="color:rgb(15, 17, 21);">整理必须加注释的地方</font>

+ <font style="color:rgb(15, 17, 21);">公共组件使用说明</font>
+ <font style="color:rgb(15, 17, 21);">api 目录的接口 js 文件必须加注释</font>
+ <font style="color:rgb(15, 17, 21);">store 中的 state, mutation, action 等必须加注释</font>
+ <font style="color:rgb(15, 17, 21);">vue 文件中的 template 必须加注释，若文件较大添加 start end 注释</font>
+ <font style="color:rgb(15, 17, 21);">vue 文件的 methods，每个 method 必须添加注释</font>
+ <font style="color:rgb(15, 17, 21);">vue 文件的 data，非常见单词要加注释</font>

#### <font style="color:rgb(15, 17, 21);">其他</font>
1. <font style="color:rgb(15, 17, 21);">尽量不要手动操作 DOM</font>

<font style="color:rgb(15, 17, 21);">因使用 vue 框架，所以在项目开发中尽量使用 vue 的数据驱动更新 DOM，尽量（不到万不得已）不要手动操作 DOM，包括：增删改 dom 元素、以及更改样式、添加事件等。</font>

1. <font style="color:rgb(15, 17, 21);">删除无用代码</font>

<font style="color:rgb(15, 17, 21);">因使用了 git/svn 等代码版本工具，对于无用代码必须及时删除，例如：一些调试的 console 语句、无用的弃用功能代码。</font>

