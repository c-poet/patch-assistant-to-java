package cn.cpoet.patch.assistant.fernflower;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

/**
 * @author CPoet
 */
public class FernflowerLogger extends IFernflowerLogger {
    @Override
    public void writeMessage(String s, Severity severity) {
        System.out.println(s);
    }

    @Override
    public void writeMessage(String s, Severity severity, Throwable throwable) {
        System.out.println(s);
    }
}
