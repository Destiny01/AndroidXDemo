package com.jinnuojiayin.recorddemo.util;

import java.io.File;

public class FileUtils {

    /**
     * 重命名
     */
    public static File renameFile(File srcFile, String newName) {

        File destFile = new File(newName);
        srcFile.renameTo(destFile);

        return destFile;
    }

}
