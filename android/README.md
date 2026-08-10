# 赞美诗中心 · 安卓壳（APK 打包工程）

这是一个把「赞美诗智能整理中心（手机采集端）」封装成安卓 App 的 WebView 工程。
打开后默认加载手机采集端（拍照/相册/快速录入）；首次启动可改网址。

## 打包成 APK（两步）

1. **安装 Android Studio**（免费）：https://developer.android.com/studio
   - 首次启动会自动装好 SDK 与 JDK（需联网，约 1-2GB）
2. **打开本文件夹 → 等同步完成 → Build → Build APK(s)**
   - 菜单：`Build > Build Bundle(s) / APK(s) > Build APK(s)`
   - 生成的 APK 在：`app/build/outputs/apk/debug/app-debug.apk`
   - 用数据线/微信发到安卓手机，允许“安装未知来源应用”即可安装

> 手机相机/相册权限：首次启动会请求；安装后拍照、选相册都能用。
> 换网址：首次启动弹出的输入框改；之后想改 = 在手机设置里清掉该 App 数据再打开。

## 注意

- 这是“网页封装”，需要电脑端服务 + 公网隧道在线才能用；数据存在电脑上
- 公网地址若变化：清 App 数据 → 重新输入新网址
- 正式发布（上架应用商店）需注册开发者账号并签名；自用/发给同工可直接用 debug APK

## 目录
```
app/src/main/java/com/hymncenter/app/MainActivity.java   # WebView + 相机/相册上传
app/src/main/AndroidManifest.xml                        # 权限与配置
app/src/main/res/                                       # 图标/主题/FileProvider
```
