package com.jinnuojiayin.recorddemo.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by xyl on 2018/12/14.
 */
public class AudioCutUtil {

    private static int bitNum = 16;
    private static int sampleRate = 44100;
    private static int channels = 1;

    /**
     * 裁剪音频
     *
     * @param pcmPath      pcm文件
     * @param cutStartTime 裁剪开始时间
     * @param cutEndTime   裁剪结束时间
     */
    public static void cutAudio(String pcmPath, float cutStartTime, float cutEndTime) {
        if (cutStartTime >= cutEndTime) {
            return;
        }
        RandomAccessFile srcFis = null;
        RandomAccessFile newFos = null;
        String tempOutPath = pcmPath + ".temp";
        try {

            //创建输入流
            srcFis = new RandomAccessFile(pcmPath, "rw");
            newFos = new RandomAccessFile(tempOutPath, "rw");
            //源文件开始读取位置，结束读取文件，读取数据的大小
            final int cutStartPos = getPositionFromWave(cutStartTime);
            final int cutEndPos = getPositionFromWave(cutEndTime);
            final int contentSize = cutEndPos - cutStartPos;
            //移动到文件开始读取处
            srcFis.seek(cutStartPos);
            //复制裁剪的音频数据
            copyData(srcFis, newFos, contentSize);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            //关闭输入流
            if (srcFis != null) {
                try {
                    srcFis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (newFos != null) {
                try {
                    newFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 删除源文件,
        new File(pcmPath).delete();
        //重命名为源文件
        FileUtils.renameFile(new File(tempOutPath), pcmPath);

    }

    /**
     * 获取pcm文件某个时间对应的数据位置
     *
     * @param time 时间
     * @return
     */
    private static int getPositionFromWave(float time) {
        int byteNum = bitNum / 8;
        int position = (int) (time * sampleRate * channels * byteNum);
        position = position / (byteNum * channels) * (byteNum * channels);
        return position;
    }

    /**
     * 复制数据
     *
     * @param fis      源输入流
     * @param fos      目标输出流
     * @param copySize 复制大小
     */
    private static void copyData(RandomAccessFile fis, RandomAccessFile fos, final int copySize) {
        byte[] buffer = new byte[2048];
        int length;
        int totalReadLength = 0;
        try {
            while ((length = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
                totalReadLength += length;
                int remainSize = copySize - totalReadLength;
                if (remainSize <= 0) {
                    //读取指定位置完成
                    break;
                } else if (remainSize < buffer.length) {
                    //离指定位置的大小小于buffer的大小,换remainSize的buffer
                    buffer = new byte[remainSize];
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
