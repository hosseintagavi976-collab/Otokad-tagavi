package com.example.simplecad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = findViewById(R.id.drawingView);

        Button btnLine = findViewById(R.id.btnLine);
        Button btnRect = findViewById(R.id.btnRect);
        Button btnCircle = findViewById(R.id.btnCircle);
        Button btnText = findViewById(R.id.btnText);
        Button btnDim = findViewById(R.id.btnDim);
        Button btnSnap = findViewById(R.id.btnSnap);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnSave = findViewById(R.id.btnSave);

        btnLine.setOnClickListener(v -> drawingView.setTool("line"));
        btnRect.setOnClickListener(v -> drawingView.setTool("rect"));
        btnCircle.setOnClickListener(v -> drawingView.setTool("circle"));
        btnText.setOnClickListener(v -> showTextDialog());
        btnDim.setOnClickListener(v -> drawingView.setTool("dim"));
        btnSnap.setOnClickListener(v -> drawingView.toggleSnap());
        btnClear.setOnClickListener(v -> drawingView.clearDrawing());
        btnSave.setOnClickListener(v -> checkPermissionAndSave());
    }

    private void showTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("متن را وارد کنید");

        final EditText input = new EditText(this);
        input.setHint("مثال: سوله ۱۲ متری");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("تایید", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                drawingView.setTextTool(text);
            }
        });
        builder.setNegativeButton("لغو", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveDrawing();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            } else {
                saveDrawing();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDrawing();
            } else {
                Toast.makeText(this, "دسترسی ذخیره‌سازی داده نشد", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDrawing() {
        Bitmap bitmap = drawingView.getBitmap();
        String fileName = "shed_plan_" + System.currentTimeMillis() + ".png";
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SimpleCad");
        if (!path.exists()) path.mkdirs();
        File file = new File(path, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "ذخیره شد: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطا در ذخیره", Toast.LENGTH_SHORT).show();
        }
    }
}
