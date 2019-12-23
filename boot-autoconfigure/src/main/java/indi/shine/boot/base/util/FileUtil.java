package indi.shine.boot.base.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author xiezhenxiang 2019/12/20
 */
public class FileUtil {

    /**
     * 把文件压缩成zip或rar，调用后要关闭流
     * @author xiezhenxiang 2019/12/20
     * @param sourceFile sourceFile
     * @param zos zipOutPutStream
     * @param rootDir 自定义zip内的根目录，可以不传
     **/
    public static void compressToZip(File sourceFile, ZipOutputStream zos, String rootDir) {

        byte[] buf = new byte[1024 * 1024];
        String entryName = sourceFile.getName();
        if (StringUtils.isNotBlank(rootDir)) {
            entryName = rootDir + "/" + entryName;
        }
        try {
            if (sourceFile.isFile()) {
                zos.putNextEntry(new ZipEntry(entryName));
                int len;
                FileInputStream in = new FileInputStream(sourceFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            } else {
                File[] listFiles = sourceFile.listFiles();
                if (listFiles == null || listFiles.length == 0) {
                    zos.putNextEntry(new ZipEntry(entryName + "/"));
                    zos.closeEntry();
                } else {
                    for (File file : listFiles) {
                        compressToZip(file, zos, entryName);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解压文件
     * @author xiezhenxiang 2019/12/21
     **/
    public static void decompressZip(String inputFilePath, Charset charset, String outputDir) {

        try {
            ZipFile zipFile = new ZipFile(inputFilePath, charset);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    String dirPath = outputDir + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    File targetFile = new File(outputDir + "/" + entry.getName());
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024 * 1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                    is.close();
                }
            }
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
