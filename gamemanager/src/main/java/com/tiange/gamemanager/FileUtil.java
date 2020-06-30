package com.tiange.gamemanager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: hqs
 * Date: 2016/4/13
 * Time: 21:44
 */
public class FileUtil {

    public static final int IMAGE = 1;
    public static final int VIDEO = 2;

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        try {
            File noMedia = new File(folder, ".nomedia");  //在当前文件夹底下创建一个 .nomedia 文件
            if (!noMedia.exists()) noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    //解决Android 5.0以下delete之后又mkdirs总是false的系统bug
    public static void renameAndDelete(File fileOrDirectory) {
        File newFile = new File(fileOrDirectory.getParent() + File.separator
                + "_" + fileOrDirectory.getName());
        fileOrDirectory.renameTo(newFile);
        delete(newFile);
    }

    public static void delete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                delete(child);
        fileOrDirectory.delete();
    }

    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static File getCacheFileByType(Context context, String type) {
        File file = null;
        if (existSDCard()) {
            file = ContextCompat.getExternalCacheDirs(context)[0];
        }
        if (file == null) {
            file = context.getCacheDir();
        }
        file = new File(file.getAbsolutePath() + File.separator + type);
        return file;
    }

    public static long getFolderSize(File file) {
        long size = 0;
        if (file == null)
            return 0;
        File[] files = file.listFiles();
        if (files == null || file.length() == 0)
            return 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size += getFolderSize(files[i]);
            } else {
                size += files[i].length();
            }
        }
        return size;
    }

    public static String getFileSize(long size) {
        DecimalFormat df = new DecimalFormat("###.##");
        float f = ((float) size / (float) (1024 * 1024));
        if (f < 1.0) {
            float f2 = ((float) size / (float) (1024));
            return df.format(Float.valueOf(f2).doubleValue()) + "KB";
        } else {
            return df.format(Float.valueOf(f).doubleValue()) + "M";
        }
    }

    public static void unzipFile(String zipFile, String targetDir) {
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String strEntry; //保存每个zip的条目名称

        FileInputStream fis = null;
        ZipInputStream zis = null;
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; //每个zip条目的实例
            while ((entry = zis.getNextEntry()) != null) {
                FileOutputStream fos = null;
                BufferedOutputStream dest = null; //缓冲输出流
                try {
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists() && !entryDir.mkdirs()) continue;

                    if (entry.isDirectory() && !entryFile.exists()) {
                        entryFile.mkdir();
                        continue;
                    }

                    fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    IOUtil.close(fos, dest);
                }
            }
        } catch (Exception cwj) {
            cwj.printStackTrace();
        } finally {
            IOUtil.close(fis, zis);
        }
    }

    public static boolean saveBitmap(Bitmap bitmap, String filePath) {
        File saveFile = new File(filePath);
        File fileDir = new File(saveFile.getParent());
        if (fileDir.exists() || fileDir.mkdirs()) {
            File vFile = new File(filePath);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(vFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.close(out);
            }
        }
        return false;
    }

    /**
     * 获取当前存放图片，相册的位置
     * fileType  文件类型 1 : 图片类型    2：视频类型
     *
     * @param context
     * @return
     */
    public static String getSkinGiftPath(Context context, int fileType) {
        String type;
        if (fileType == IMAGE) {
            type = Environment.DIRECTORY_DCIM;
        } else if (fileType == VIDEO) {
            type = Environment.DIRECTORY_MOVIES;
        } else {
            type = Environment.DIRECTORY_DOWNLOADS;
        }

        File file = context.getExternalFilesDir(type);
        return file.getAbsolutePath();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }


    //根据uri获取文件绝对路径
    public static String getPathFromUri(Context context, Uri uri) {
        final String column = MediaStore.Images.ImageColumns.DATA;
        final String[] projection = {column};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        String result = null;
        if (cursor.moveToFirst()) {
            final int columnIndex = cursor.getColumnIndex(column);
            result = cursor.getString(columnIndex);
        }
        cursor.close();
        return result;
    }
}
