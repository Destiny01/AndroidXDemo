package com.jinnuojiayin.audioedit.callback;

/**
 * 音频解码监听器
 */
public interface DecodeOperateInterface {
    void updateDecodeProgress(int decodeProgress);

    void decodeSuccess();

    void decodeFail();
}
