package org.tangyang.hyperledgerfabric.app.javademo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class AppendObjectOutputStream extends ObjectOutputStream {
    private static File file = null;

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        AppendObjectOutputStream.file = file;
    }


    public AppendObjectOutputStream(File file) throws IOException {
        super(new FileOutputStream(file, true));
    }

    @Override
    public void writeStreamHeader() throws IOException {
        // 如果文件为空直接写入 StreamHeader
        if (file == null || file.length() == 0) {
            super.writeStreamHeader();
        } else {
            // 文件中存在内容，则说明文件中已经存在了一个 StreamHeader，调用父类的 reset() 方法保证文件中只存在一个 StreamHeader
            this.reset();
        }
    }
}
