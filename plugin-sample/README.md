# BiliPai 示例插件项目

这是一个示例项目，演示如何创建可通过 URL 动态加载的 BiliPai 外部插件 (.bpx)。

## 项目结构

```
plugin-sample/
├── README.md
├── build.gradle.kts
├── manifest.json
└── src/main/java/com/bilipai/plugins/
    └── DurationFilterPlugin.kt
```

## 构建步骤

### 1. 编写插件代码

参见 `src/main/java/com/bilipai/plugins/DurationFilterPlugin.kt`

### 2. 编译

```bash
./gradlew :plugin-sample:assembleRelease
```

### 3. 提取 DEX

```bash
cp build/intermediates/dex/release/minifyReleaseWithR8/classes.dex ./
```

### 4. 打包 .bpx

```bash
zip -r duration_filter.bpx manifest.json classes.dex
```

### 5. 上传并分发

上传 `duration_filter.bpx` 到 GitHub Releases 或其他公开 URL。

用户在 BiliPai 中输入 URL 即可安装。

## 插件接口

插件必须实现以下接口之一：

- `Plugin` - 通用插件
- `FeedPlugin` - 首页推荐过滤
- `PlayerPlugin` - 视频播放增强
- `DanmakuPlugin` - 弹幕处理

详见 [插件开发指南](../docs/PLUGIN_DEVELOPMENT.md)
