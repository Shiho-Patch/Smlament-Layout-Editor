# 可视化布局器（Material 3 / Kotlin）

一个在 Android 设备上运行的 **Android 布局可视化编辑器。

## 功能一览

- **控件库（Widget Palette）**：按分类展示常用的 Android 视图与布局，点击即插入当前选中节点。
- **实时画布（Canvas）**：实时渲染当前 XML 树，点击可选中任意节点，选中后有描边高亮。
- **属性面板（Attributes）**：编辑当前选中节点的常用属性。
- **XML 代码视图**：实时生成 XML 文本，支持复制到剪贴板。
- **Material Design 3**（Material You）风格主题，自动支持深/浅色。

## 技术栈

- **语言**：Kotlin 2.0
- **UI**：Material Design 3 (`com.google.android.material:material:1.12.0`)
- **架构**：Activity + Fragment + ViewBinding + ViewPager2
- **最低 SDK**：24 (Android 7.0+)
- **目标 SDK**：34

## 目录结构

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/layoutbuilder/
│   ├── MainActivity.kt                    # 欢迎页
│   ├── designer/
│   │   ├── DesignerActivity.kt          # 布局编辑器主界面
│   │   ├── DesignerState.kt             # 全局状态（树 + 选中）
│   │   ├── widget/WidgetPaletteFragment.kt
│   │   ├── canvas/DesignerCanvasFragment.kt
│   │   ├── attributes/AttributesFragment.kt
│   │   └── code/XmlCodeFragment.kt
│   ├── model/
│   │   ├── ViewNode.kt                # 视图节点（id/tag/属性
│   │   └── WidgetLibrary.kt             # 控件库模板
│   └── parser/
│       ├── XmlToViewBuilder.kt         # 树 → Android View
│       └── ViewToXmlBuilder.kt         # 树 → XML 文本
└── res/
    ├── layout/                          # 布局 XML
    ├── menu/                            # 菜单
    ├── values/ & values-night/             # 颜色/主题/字符串/尺寸
    ├── drawable/
    └── mipmap-anydpi-v26/
```

## 构建方式

### 用 Android Studio 打开此项目根目录，或使用命令行：

```
./gradlew assembleDebug
```

> 如果仓库未包含 gradlew，可执行：
```
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

生成的 APK 在 `app/build/outputs/apk/debug/app-debug.apk`。

## 核心设计要点

### 状态管理
- `DesignerState` 是一个 object 单例，持有根节点 `root: ViewNode` 与 `selectedId`。
- 通过 `ChangeListener` 监听树与选中项变更，四个 fragment 订阅后刷新界面。

### 视图节点模型
每个 `ViewNode` 包含 tag、属性 Map、子节点 List、parent 引用，形成可插拔的结构，可以任意层级。

### 渲染
`XmlToViewBuilder` 递归遍历节点树，动态实例化对应 `android.view.View` 子类（`LinearLayout`、`TextView`、`MaterialButton` 等）。解析常见布局属性（宽/高/margin/padding/text/textSize/background/orientation/gravity/visibility/alpha/elevation/src/checked/enabled/clickable…）。

未实现的属性会被安全忽略，不会崩溃。

### 序列化
`ViewToXmlBuilder` 递归生成 XML 文本，自动加命名空间声明，按插入顺序输出属性。

## 扩展方向

- 拖拽操作（拖放控件与拖动顺序）
- 撤销/重做
- 保存/加载布局到本地文件
- 更多属性类型安全编辑
