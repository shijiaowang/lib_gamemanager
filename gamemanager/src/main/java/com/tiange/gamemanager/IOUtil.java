package com.tiange.gamemanager;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: ljx
 * Date: 2016/11/15
 * Time: 15:31
 */
public class IOUtil {

    private static final int LENGTH_BYTE = 8 * 1024;//一次性读写的字节个数，用于字节读取

    public static int copy(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(in, out);
        }
        return count;
    }

    /**
     * 根据文件路径获取文件里面的内容
     *
     * @param filePath 文件路径
     * @return 文件里面的内容
     */
    public static String read(String filePath) {
        return read(new File(filePath));
    }

    /**
     * 根据文件对象获取文本文件里面的的内容
     *
     * @param file 文件对象
     * @return 文件里面的内容
     */
    public static String read(File file) {
        try {
            if (!isFile(file)) return null;
            return read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取输入流里面的内容，并按String类型返回结果
     *
     * @param is 要读取的流
     * @return 读取的结果
     */
    public static String read(InputStream is) {
        try {
            if (is == null) return null;
            StringBuilder stringBuffer = new StringBuilder();
            int length;
            byte[] bytes = new byte[LENGTH_BYTE];
            while ((length = is.read(bytes)) != -1) {
                stringBuffer.append(new String(bytes, 0, length));
            }
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(is);
        }
        return null;
    }

    public static boolean write(InputStream is, String path) {
        return !TextUtils.isEmpty(path) && write(is, new File(path));
    }

    /**
     * 读取流里面的内容，并以文件的形式保存
     *
     * @param is      要读取的流
     * @param dstFile 保存的目标文件对象
     */
    public static boolean write(InputStream is, File dstFile) {
        if (dstFile == null) return false;
        File parentFile = dstFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) return false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dstFile);
            return write(is, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(is, fos);
        }
        return false;
    }


    public static boolean write(InputStream is, OutputStream os) {
        if (is == null || os == null) return false;
        try {
            byte[] bytes = new byte[LENGTH_BYTE];
            int readLength;
            while ((readLength = is.read(bytes, 0, bytes.length)) != -1) {
                os.write(bytes, 0, readLength);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(is, os);
        }
        return false;

    }


    /**
     * 判断文件对象是否是一个文件，不是或者不存在则引发非法参数异常
     *
     * @param dir 文件对象
     */
    private static boolean isFile(File dir) {
        return dir.exists() && dir.isFile();
    }

    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) return;
        for (Closeable closeable : closeables) {
            if (closeable == null) continue;
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
