package com.nuvoton.skyeye;

/**
 * Created by timsheu on 4/25/16.
 */

public class FileContent {
        public final String fileSerial;
        public final String fileName;
        public final String fileDate;

        public FileContent(String fileName, String fileDate, String fileSerial) {
            this.fileName = fileName;
            this.fileDate = fileDate;
            this.fileSerial = fileSerial;
        }

        @Override
        public String toString() {
            return fileName;
        }
}

