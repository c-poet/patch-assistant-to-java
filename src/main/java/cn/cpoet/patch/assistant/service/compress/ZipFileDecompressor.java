package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.constant.CharsetConst;
import cn.cpoet.patch.assistant.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author CPoet
 */
public class ZipFileDecompressor extends FileDecompressor {

    public static final ZipFileDecompressor INSTANCE = new ZipFileDecompressor();

    @Override
    public void decompress(InputStream in, UnCallback callback) {
        try (ZipInputStream zin = new ZipInputStream(in, CharsetConst.GBK)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                callback.invoke(entry, zin);
            }
        } catch (IOException e) {
            throw new AppException("Decompress the zip file failed", e);
        }
    }
}
