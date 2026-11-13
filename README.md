# Teleprompter 提词器

一个纯本地、无广告、无内购、不需要登录注册的 Android 提词器 App。

## 项目初衷

市面上的提词器 App 大多逃不开三件事：**要充值会员才能用核心功能、塞满广告、强制登录收集数据**。

我只是一个想自己顺手用的人，需求其实很简单：

- 不想充值，不想被会员墙卡住；
- 只想要一个**纯粹的、好用的**提词器；
- 方便自己随时导入文字、照着念就行。

所以这个项目从一开始就把方向定死：**不联网、不上传、不追踪、不收费**。所有脚本都存在你手机本地，App 打开即用，没有任何账号体系。

如果你也只想安安静静地用个提词器，欢迎直接拿去用。

## 功能特性

- 本地脚本管理：新建 / 编辑 / 删除 / 导入（txt）提词文本
- 播放页：可调速、调字号、换颜色、护眼模式、镜像翻转、浮窗模式
- 双指捏合缩放字号
- 全局崩溃捕获：万一出错会自动记录日志，下次打开可查看并复制上报
- 纯本地运行，无任何网络请求、无广告、无内购

## 下载安装

最新版本在 GitHub Releases 发布（仓库已设为公开）：

https://github.com/BD7OVY/TeleprompterApp/releases

下载 `Teleprompter.apk` 安装即可。安装时若提示「未知来源」，按系统引导允许本次安装即可。

> 因为 GitHub Release 不支持中文文件名，APK 命名为 `Teleprompter.apk`（就是「提词器」的英文）。

## 技术栈

- Android（Kotlin）+ ViewBinding
- minSdk 26 / targetSdk 34
- Gradle 8.5，JDK 17（Temurin）

## 自动构建（CI）

仓库通过 GitHub Actions 自动出包，无需在本机安装 Android 开发环境：

- `build.yml`：每次 push 自动构建 **Debug** APK 作为产物（artifact）
- `release.yml`：通过 `workflow_dispatch` 手动触发，使用固定签名密钥构建 **Release** 签名 APK，并自动发版到 GitHub Releases（支持原地覆盖升级）

## 隐私说明

App 不会收集任何个人信息，不会发起任何网络请求。你的所有提词内容只保存在本机。

---

Made for personal use. No ads. No subscriptions. No accounts.
