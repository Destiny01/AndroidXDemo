package com.jinnuojiayin.recorddemo.util;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 录制
 * Created by parcool on 2017/11/30.
 */

public class RecorderUtil {
    private static final RecorderUtil ourInstance = new RecorderUtil();

    public static RecorderUtil getInstance() {
        return ourInstance;
    }

    private RecorderUtil() {
    }


    public interface OnVolumeChangeListener {
        void onVolumeChanged(int readSize, double volume);
    }

    private AudioRecord mAudioRecord;
    private int audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes;
    private static final int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};
    private boolean isRecording = false;
    private String mFilePath;
    private Thread recordingThread = null;
    private OnVolumeChangeListener onVolumeChangeListener;
    private boolean isReallyRecord;//是否正在录音mic

    public boolean isReallyRecord() {
        return isReallyRecord;
    }

    public void setReallyRecord(boolean reallyRecord) {
        isReallyRecord = reallyRecord;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public int getBufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    /**
     * 开始录制
     *
     * @param audioSource
     * @param filePath
     * @param append
     * @param onVolumeChangeListener
     */
    public void startRecording(int audioSource, String filePath, final boolean append, OnVolumeChangeListener onVolumeChangeListener) {
        if (isRecording) {
            return;
        }
        this.onVolumeChangeListener = onVolumeChangeListener;
        this.mFilePath = filePath;
        if (mAudioRecord == null) {
            mAudioRecord = findAudioRecord(audioSource);
        }
        mAudioRecord.startRecording();
        isRecording = true;
        if (recordingThread != null && !recordingThread.isInterrupted() && recordingThread.isAlive()) {
            recordingThread.interrupt();
            recordingThread = null;
        }
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(append);
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (recordingThread != null && recordingThread.isAlive() && !recordingThread.isInterrupted()) {
            recordingThread.interrupt();
            recordingThread = null;
        }
        // stops the recording activity
        if (null != mAudioRecord) {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

    }

    boolean isRecording() {
        return isRecording;
    }

    /**
     * 将录制的数据写入到文件
     *
     * @param append
     */
    private void writeAudioDataToFile(boolean append) {
        // Write the output audio in byte
        String filePath = mFilePath;
        short sData[] = new short[bufferSizeInBytes / 2];
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath, append);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (os == null) {
            return;
        }
        synchronized (this) {
            while (isRecording) {
                // gets the voice output from microphone to byte format
                int readSize = mAudioRecord.read(sData, 0, bufferSizeInBytes / 2);
                if (isReallyRecord) {
                    calculateRealVolume(sData, readSize);
                    try {
                        // writes the data to file from buffer stores the voice buffer
                        byte bData[] = short2byte(sData);
                        os.write(bData, 0, bufferSizeInBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    byte[] bytes = new byte[sData.length * 2];
                    try {
                        os.write(bytes, 0, bufferSizeInBytes);
                        if (onVolumeChangeListener != null) {
                            onVolumeChangeListener.onVolumeChanged(bytes.length, 0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 计算录音的实际音量
     *
     * @param mPCMBuffer
     * @param readSize
     */
    private void calculateRealVolume(short[] mPCMBuffer, int readSize) {
        long v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < mPCMBuffer.length; i++) {
            v += mPCMBuffer[i] * mPCMBuffer[i];
        }
        // 平方和除以数据总长度，得到音量大小。
        double mean = v / (double) readSize;
        double volume = 10 * Math.log10(mean);
        if (onVolumeChangeListener != null) {
            onVolumeChangeListener.onVolumeChanged(mPCMBuffer.length * 2, volume);
        }
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    /**
     * 实例化AudioRecord
     *
     * @param audioSource
     * @return
     */
    private AudioRecord findAudioRecord(int audioSource) {
        int bufferSizeTemp = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSizeTemp != AudioRecord.ERROR_BAD_VALUE) {
            AudioRecord recorderTemp = new AudioRecord(audioSource, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeTemp);
            this.sampleRateInHz = 44100;
            this.channelConfig = AudioFormat.CHANNEL_IN_MONO;
            this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            this.bufferSizeInBytes = bufferSizeTemp;
            return recorderTemp;
        }

        this.audioSource = audioSource;
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize > 0 && bufferSize <= 256) {
                            bufferSize = 256;
                        } else if (bufferSize > 256 && bufferSize <= 512) {
                            bufferSize = 512;
                        } else if (bufferSize > 512 && bufferSize <= 1024) {
                            bufferSize = 1024;
                        } else if (bufferSize > 1024 && bufferSize <= 2048) {
                            bufferSize = 2048;
                        } else if (bufferSize > 2048 && bufferSize <= 4096) {
                            bufferSize = 4096;
                        } else if (bufferSize > 4096 && bufferSize <= 8192) {
                            bufferSize = 8192;
                        } else if (bufferSize > 8192 && bufferSize <= 16384) {
                            bufferSize = 16384;
                        } else {
                            bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        }

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(audioSource, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                this.sampleRateInHz = rate;
                                this.channelConfig = channelConfig;
                                this.audioFormat = audioFormat;
                                this.bufferSizeInBytes = bufferSize;
                                return recorder;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
