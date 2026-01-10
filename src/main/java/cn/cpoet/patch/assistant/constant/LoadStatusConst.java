package cn.cpoet.patch.assistant.constant;

/**
 * 加载状态定义
 *
 * @author CPoet
 */
public interface LoadStatusConst {
    /** 无加载任务 */
    int NOT_LOAD = 0;

    /** 应用树加载中 */
    int APP_TREE_LOADING = 1;

    /** 补丁树加载中 */
    int PATCH_TREE_LOADING = 1 << 1;
}
