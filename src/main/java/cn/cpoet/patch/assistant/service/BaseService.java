package cn.cpoet.patch.assistant.service;

/**
 * 基类
 *
 * @author CPoet
 */
public interface BaseService {

    /**
     * 初始化实例
     */
    default void init() {
    }

    /**
     * 释放实例
     */
    default void destroy() {
    }
}
