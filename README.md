# FutureMC

以 **Minecraft 1.21.1** 为基础的 NeoForge 模组，目标是将**高于 1.21.1 的我的世界原版更新内容**（方块、物品、机制等）整合进模组之中，让旧版本也能体验到新版本的原版内容。

## 构建

在项目根目录执行：

```
gradlew build
```

打包产物输出到 `build/libs/` 目录。

## Jar 命名规则

打包产物文件名格式为：

```
<模组标识>-<目标 Minecraft 版本>-<mod 自身版本>.jar
```

- **`-` 前的版本号**：对应目标 Minecraft 版本，**省略 `1.` 前缀**（例如 Minecraft 1.21.1 写作 `21.1`）；
- **`-` 后的版本号**：mod 自身版本。

示例：`futuremc_neoforge-21.1-1.0.0.jar` 表示基于 Minecraft 1.21.1、mod 自身版本为 1.0.0。

## 许可证

本项目基于 **GNU AGPL 3.0** 协议开源，详见 [LICENSE](LICENSE)。
