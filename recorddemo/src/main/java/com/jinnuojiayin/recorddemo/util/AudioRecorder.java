package com.jinnuojiayin.recorddemo.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xyl
 * 直接录制aac格式的文件：
 * AudioRecord+编码器MediaCodec
 * 编码格式：A_AAC/MPEG4/LC/PS
 * 采样率：44100 HZ
 * 声道：单声道
 * 比特率：96000
 */

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private int SAMPLE_RATE = 44100; //采样率 44100
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO; //音频通道(单声道)
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //音频格式
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;  //音频源（麦克风）
    private String encodeType = MediaFormat.MIMETYPE_AUDIO_AAC;
    private boolean is_recording = false;
    private static final int samples_per_frame = 2048;
    public static AudioRecord audioRecord;
    private Thread recorderThread;
    private MediaCodec mediaEncode;
    private MediaCodec.BufferInfo encodeBufferInfo;
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private byte[] chunkAudio = new byte[0];
    private File f = new File(Environment.getExternalStorageDirectory(), "MyAudio.aac");
    private BufferedOutputStream out;
    private RecorderTask recorderTask;

    public AudioRecorder() {
        initAACMediaEncode();
        recorderTask = new RecorderTask();
    }

    /**
     * 开始录制
     * 开启子线程录制
     */
    public void startAudioRecording() {
        recorderThread = new Thread(recorderTask);
        if (!recorderThread.isAlive()) {
            recorderThread.start();
        }
    }

    /**
     * 停止录制
     */
    public void stopAudioRecording() {
        try {
            //释放回声消除器
            releaseAEC();
            releaseNC();
            is_recording = false;
            mediaEncode.stop();
            mediaEncode.release();

            if (recorderThread != null && recorderThread.isAlive() && !recorderThread.isInterrupted()) {
                recorderThread.interrupt();
                recorderThread = null;
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    class RecorderTask implements Runnable {
        int bufferReadResult = 0;

        public RecorderTask() {
            try {
                out = new BufferedOutputStream(new FileOutputStream(f, false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            //获取最小缓冲区大小
            int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            Log.e(TAG, bufferSizeInBytes + "");
            //缓冲区最好设置为2倍以上的大小，防止录制出来的音频数据播放时卡顿等问题，
            // 这里主要要注意采样率和音频通道，有可能会因为音频通道设置不正确造成音频播放异常
            audioRecord = new AudioRecord(
                    AUDIO_SOURCE,   //音频源
                    SAMPLE_RATE,    //采样率
                    CHANNEL_CONFIG,  //音频通道
                    AUDIO_FORMAT,    //音频格式\采样精度
                    bufferSizeInBytes * 4//缓冲区
            );
            Log.e(TAG, "是否支持回声消除" + isDeviceSupportAEC());
            if (isDeviceSupportAEC()) {
                initAEC(audioRecord.getAudioSessionId());
            }
            Log.e(TAG, "是否支持噪声抑制" + isDeviceSupportNC());
            if (isDeviceSupportNC()) {
                initNC(audioRecord.getAudioSessionId());
            }
            audioRecord.startRecording();
            is_recording = true;
            while (is_recording) {
                byte[] buffer = new byte[samples_per_frame];
                //从缓冲区中读取数据，存入到buffer字节数组数组中
                bufferReadResult = audioRecord.read(buffer, 0, buffer.length);
                //判断是否读取成功
                if (bufferReadResult == android.media.AudioRecord.ERROR_BAD_VALUE || bufferReadResult == android.media.AudioRecord.ERROR_INVALID_OPERATION)
                    Log.e(TAG, "Read error");
                if (audioRecord != null && bufferReadResult > 0) {
                    Log.i(TAG, bufferReadResult + "");
                    try {
                        dstAudioFormatFromPCM(buffer);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

            }
            if (audioRecord != null) {
                audioRecord.setRecordPositionUpdateListener(null);
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }


    /**
     * 初始化AAC编码器
     * 32 kbps —MW(AM) 质量
     * 96 kbps —FM质量
     * 128 - 160 kbps –相当好的质量，有时有明显差别
     * 192 kbps — 优良质量，偶尔有差别
     * 224 - 320 kbps — 高质量
     */
    private void initAACMediaEncode() {
        try {
            //参数对应-> mime type、采样率、声道数
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(
                    encodeType,
                    44100,
                    1);
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);//比特率
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, samples_per_frame);//作用于inputBuffer的大小
            mediaEncode = MediaCodec.createEncoderByType(encodeType);
            mediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaEncode == null) {
            Log.e(TAG, "create mediaEncode failed");
            return;
        }
        mediaEncode.start();
        encodeInputBuffers = mediaEncode.getInputBuffers();
        encodeOutputBuffers = mediaEncode.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();
    }


    /**
     * 编码PCM数据 得到AAC格式的音频文件
     */
    private void dstAudioFormatFromPCM(byte[] pcmData) {
        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;
        int outBitSize;
        int outPacketSize;
        byte[] PCMAudio;
        PCMAudio = pcmData;
        encodeInputBuffers = mediaEncode.getInputBuffers();
        encodeOutputBuffers = mediaEncode.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();

        inputIndex = mediaEncode.dequeueInputBuffer(0);
        inputBuffer = encodeInputBuffers[inputIndex];
        inputBuffer.clear();
        inputBuffer.limit(PCMAudio.length);
        inputBuffer.put(PCMAudio);//PCM数据填充给inputBuffer
        mediaEncode.queueInputBuffer(inputIndex, 0, PCMAudio.length, 0, 0);//通知编码器 编码
        outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 0);
        while (outputIndex > 0) {
            outBitSize = encodeBufferInfo.size;
            outPacketSize = outBitSize + 7;//7为ADT头部的大小
            outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
            outputBuffer.position(encodeBufferInfo.offset);
            outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
            chunkAudio = new byte[outPacketSize];
            addADTStoPacket(chunkAudio, outPacketSize);//添加ADTS
            outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中
            try {
                //录制aac音频文件，保存在手机内存中
                out.write(chunkAudio, 0, chunkAudio.length);
                out.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputBuffer.position(encodeBufferInfo.offset);
            mediaEncode.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 0);
        }
    }


    /**
     * 添加ADTS头
     * 采样率freqIdx参数：
     * <p>
     * 0: 96000 Hz
     * 1: 88200 Hz
     * 2: 64000 Hz
     * 3: 48000 Hz
     * 4: 44100 Hz
     * 5: 32000 Hz
     * 6: 24000 Hz
     * 7: 22050 Hz
     * 8: 16000 Hz
     * 9: 12000 Hz
     * 10: 11025 Hz
     * 11: 8000 Hz
     * 12: 7350 Hz
     * 13: Reserved
     * 14: Reserved
     * 15: frequency is written explictly
     * 声道数chanCfg参数： 
     * <p>
     * 0: Defined in AOT Specifc Config
     * 1: 1 channel: front-center
     * 2: 2 channels: front-left, front-right
     * 3: 3 channels: front-center, front-left, front-right
     * 4: 4 channels: front-center, front-left, front-right, back-center
     * 5: 5 channels: front-center, front-left, front-right, back-left, back-right
     * 6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
     * 7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel
     * 8-15: Reserved
     *
     * @param packet
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44100 采样率
        int chanCfg = 1; // CPE 声道数
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    //回声消除AcousticEchoCanceler 继承自AudioEffect
    //声学回声消除器（AEC）AcousticEchoCanceler类消除了从远程捕捉到音频信号上的信号的作用
    //自动增益控制（AGC）AutomaticGainControl类自动恢复正常捕获的信号输出
    //噪声抑制器（NC）NoiseSuppressor类可以消除被捕获信号的背景噪音
    //
    private static AcousticEchoCanceler canceler;//回声消除AEC
    private static NoiseSuppressor noiseSuppressor;//噪声抑制器（NC）

    public static boolean isDeviceSupportAEC() {
        return AcousticEchoCanceler.isAvailable();
    }

    public static boolean isDeviceSupportNC() {
        return NoiseSuppressor.isAvailable();
    }

    public static boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        if (canceler != null) {
            canceler.setEnabled(true);
        }
        return canceler.getEnabled();
    }

    public static boolean initNC(int audioSession) {
        if (noiseSuppressor != null) {
            return false;
        }
        noiseSuppressor = NoiseSuppressor.create(audioSession);
        if (noiseSuppressor != null) {
            noiseSuppressor.setEnabled(true);
        }
        return noiseSuppressor.getEnabled();
    }

    public static boolean setAECEnabled(boolean enable) {
        if (null == canceler) {
            return false;
        }
        canceler.setEnabled(enable);
        return canceler.getEnabled();
    }

    public static boolean setNCEnabled(boolean enable) {
        if (null == noiseSuppressor) {
            return false;
        }
        noiseSuppressor.setEnabled(enable);
        return noiseSuppressor.getEnabled();
    }

    /**
     * 释放AEC
     *
     * @return
     */
    private boolean releaseAEC() {
        if (null == canceler) {
            return false;
        }
        try {
            canceler.setEnabled(false);
            canceler.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 释放nc
     *
     * @return
     */
    private boolean releaseNC() {
        if (null == noiseSuppressor) {
            return false;
        }
        try {
            noiseSuppressor.setEnabled(false);
            noiseSuppressor.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }
}
