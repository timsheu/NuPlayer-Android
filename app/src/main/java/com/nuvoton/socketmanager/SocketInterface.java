package com.nuvoton.socketmanager;

import com.nuvoton.nuplayer.FileContent;

import java.util.ArrayList;

/**
 * Created by timsheu on 6/13/16.
 */
public interface SocketInterface {
    void showToastMessage(String message);
    void updateFileList(ArrayList<FileContent> fileList);
    void deviceIsAlive();
    void updateSettingContent(String category, String value);
}
