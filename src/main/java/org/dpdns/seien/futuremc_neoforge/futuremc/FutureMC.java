package org.dpdns.seien.futuremc_neoforge.futuremc;

// FutureMC 内容模块的公共常量。
// 所有移植内容（物品、实体、音效、数据包资源等）统一使用这里定义的命名空间 futuremc，
// 它与模组 ID（futuremc_neoforge）区分开：内容的注册键形如 futuremc:xxx，
// 资源文件放在 assets/futuremc 与 data/futuremc 目录下。
public final class FutureMC {

    // 内容命名空间，所有新注册键和资源路径都以它开头。
    public static final String NAMESPACE = "futuremc";

    // 工具类不允许被实例化。
    private FutureMC() {
    }
}
