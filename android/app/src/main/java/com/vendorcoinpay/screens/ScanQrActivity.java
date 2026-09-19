package com.vendorcoinpay.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.vendorcoinpay.AppConfig;
import com.vendorcoinpay.MainActivity;
import com.vendorcoinpay.R;
import java.util.List;

public class ScanQrActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private CompoundBarcodeView barcodeScannerView;
    private ImageView ivScanBack;
    private ImageView ivScanFlashlight;
    private AppCompatButton btnEnterManually;
    private boolean isTorchOn = false;
    private boolean hasScanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        ivScanBack = findViewById(R.id.ivScanBack);
        ivScanFlashlight = findViewById(R.id.ivScanFlashlight);
        btnEnterManually = findViewById(R.id.btnEnterManually);

        ivScanBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEnterManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivScanFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTorch();
            }
        });

        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null && !hasScanned) {
                    hasScanned = true;
                    String raw = result.getText().trim();
                    handleScannedCode(raw);
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void handleScannedCode(String rawText) {
        String receiverCode = rawText;
        if (receiverCode.startsWith(AppConfig.QR_PREFIX)) {
            receiverCode = receiverCode.replace(AppConfig.QR_PREFIX, "").trim();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("scanned_receiver_code", receiverCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void toggleTorch() {
        if (isTorchOn) {
            barcodeScannerView.setTorchOff();
            isTorchOn = false;
        } else {
            barcodeScannerView.setTorchOn();
            isTorchOn = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}
