package com.nuvoton.nuplayer;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class QRCode extends AppCompatActivity {
    private static final String TAG = "QRCode";
    ImageView qrcode;
    String string;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
//        if (w > h){
//            h -= 25;
//            w = h;
//        }
        string = getIntent().getStringExtra("QR code");
        Log.d(TAG, "onCreate: " + string + " " + String.valueOf(w) + " " + String.valueOf(h));
        qrcode = (ImageView) findViewById(R.id.qrcode);
        Bitmap bitmap = null;

        try{
            bitmap = encodeAsBitmap(string, BarcodeFormat.QR_CODE, 600, 600);
            qrcode.setImageBitmap(bitmap);
        }catch (WriterException e){
            e.printStackTrace();
        }
    }
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String content, BarcodeFormat format, int width, int height) throws WriterException{
        String contentsToEncode = content;
        if (contentsToEncode == null) return null;

        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null){
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try{
            result = writer.encode(contentsToEncode, format, width, height, hints);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int [] pixels = new int[w*h];
        for (int i=0; i<h; i++){
            int offset = i*w;
            for (int j=0; j<w; j++){
                pixels[offset + j] = result.get(i, j) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}
