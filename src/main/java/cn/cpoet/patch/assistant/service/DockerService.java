package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.core.AppContext;

/**
 * Docker
 *
 * @author CPoet
 */
public class DockerService {

    public static DockerService getInstance() {
        return AppContext.getInstance().getService(DockerService.class);
    }
}
