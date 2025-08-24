package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.exception.AppException;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import com.github.junrar.rarfile.FileHeader;

import java.io.IOException;
import java.io.InputStream;

/**
 * 注意：{@link com.github.junrar.Junrar}仅支持rar4的解压，并不支持压缩工作
 *
 * @author CPoet
 */
public class RarFileDecompressor extends FileDecompressor {

    public static final RarFileDecompressor INSTANCE = new RarFileDecompressor();

    @Override
    public void decompress(InputStream in, UnCallback callback) {
        try (Archive archive = new Archive(in)) {
            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                byte[] bytes = null;
                if (!fileHeader.isDirectory()) {
                    bytes = readBytes(archive, fileHeader);
                }
                callback.invoke(fileHeader, bytes);
            }
        } catch (UnsupportedRarV5Exception e) {
            doLocalDecompress(in, callback);
        } catch (IOException | RarException e) {
            throw new AppException("Decompress RAR files failed", e);
        }
    }

    private void doLocalDecompress(InputStream in, UnCallback callback) {
        // Rar5版本未开源需要调用本地工具处理
        try {
            in.reset();
        } catch (IOException e) {
            throw new AppException("The RAR conversion tool failed", e);
        }
        LocalFileDecompressor.INSTANCE.decompress(in, callback);
    }

    private byte[] readBytes(Archive archive, FileHeader fileHeader) throws IOException {
        try (InputStream in = archive.getInputStream(fileHeader)) {
            return in.readAllBytes();
        }
    }
}
