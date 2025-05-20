package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.service.BaseService;
import cn.cpoet.patch.assistant.util.ReflectUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工厂
 *
 * @author CPoet
 */
public class ServiceFactory {

    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        Object obj = beanMap.get(clazz);
        if (obj == null) {
            synchronized (this) {
                obj = beanMap.get(clazz);
                if (obj == null) {
                    for (Map.Entry<Class<?>, Object> entry : beanMap.entrySet()) {
                        if (clazz.isAssignableFrom(entry.getKey())) {
                            obj = entry.getValue();
                            break;
                        }
                    }
                    if (obj == null) {
                        obj = ReflectUtil.newInstance(clazz);
                        if (obj instanceof BaseService) {
                            ((BaseService) obj).init();
                        }
                    }
                    beanMap.put(clazz, obj);
                }
            }
        }
        return (T) obj;
    }

    public void destroy() {
        beanMap.forEach((k, v) -> {
            if (v instanceof BaseService) {
                ((BaseService) v).destroy();
            }
        });
    }
}
