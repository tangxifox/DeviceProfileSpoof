# DeviceProfileSpoof · 机型伪装

一个基于 LSPosed API 102 的 Android 模块，可为作用域内的应用设置制造商、品牌、型号、产品名称和设备代号。模块仅修改目标进程内的 Java 读取路径，不更改系统属性，也不覆盖原生读取路径。

- 版本：`1.0.0`（`versionCode=11`）
- 包名：`com.deviceprofile.spoof`
- Android SDK：最低 API 26，目标 API 36
- 依赖：libxposed API 和 Service 102.0.0

在 LSPosed 中启用模块并选择作用域；修改设备参数并保存后，彻底退出并重新打开目标应用。概览页的「应用作用域」可查看当前勾选项。

本仓库仅提供 Java 源码、Android 清单和 LSPosed 模块配置，不包含构建或签名脚本、签名密钥、第三方二进制依赖及 APK。

许可证：[Apache License 2.0](LICENSE)。
