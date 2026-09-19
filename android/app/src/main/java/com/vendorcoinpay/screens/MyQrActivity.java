package com.vendorcoinpay.screens;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vendorcoinpay.AppConfig;
import com.vendorcoinpay.R;
import com.vendorcoinpay.storage.SessionManager;

public class MyQrActivity extends AppCompatActivity {
    private ImageView ivMyQrBack;
    private TextView tvMyQrFullName;
    private TextView tvMyQrUserCode;
    private ImageView ivQrCodeImage;
    private ProgressBar pbQrLoading;
    private AppCompatButton btnDoneMyQr;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);

        sessionManager = new SessionManager(this);

        ivMyQrBack = findViewById(R.id.ivMyQrBack);
        tvMyQrFullName = findViewById(R.id.tvMyQrFullName);
        tvMyQrUserCode = findViewById(R.id.tvMyQrUserCode);
        ivQrCodeImage = findViewById(R.id.ivQrCodeImage);
        pbQrLoading = findViewById(R.id.pbQrLoading);
        btnDoneMyQr = findViewById(R.id.btnDoneMyQr);

        tvMyQrFullName.setText(sessionManager.getFullName());
        tvMyQrUserCode.setText(sessionManager.getUserCode());

        ivMyQrBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDoneMyQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        generateQrCode();
    }

    private void generateQrCode() {
        String userCode = sessionManager.getUserCode();
        if (userCode.isEmpty()) {
            userCode = "VC-" + sessionManager.getUserId();
        }
        final String qrPayload = AppConfig.QR_PREFIX + userCode;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QRCodeWriter writer = new QRCodeWriter();
                    BitMatrix bitMatrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbQrLoading.setVisibility(View.GONE);
                            ivQrCodeImage.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbQrLoading.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }
}
