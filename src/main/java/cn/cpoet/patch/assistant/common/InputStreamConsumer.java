package cn.cpoet.patch.assistant.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author CPoet
 */
@FunctionalInterface
public interface InputStreamConsumer {
    void accept(InputStream in) throws IOException;
}
