package cn.cpoet.patch.assistant.fernflower;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.File;
import java.io.IOException;

/**
 * @author CPoet
 */
public class BytecodeProvider implements IBytecodeProvider {

    private File file;
    private byte[] bytes;

    @Override
    public byte[] getBytecode(String s, String s1) throws IOException {
        return bytes;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
