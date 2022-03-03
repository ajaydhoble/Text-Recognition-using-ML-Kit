package com.example.textrecog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class RecogActivity extends AppCompatActivity {

    Button captBtn, recogBtn;
    ImageView imageView;
    Bitmap bitmap;
    final static int IMAGE_REQ_CODE = 100;
    TextView textView;
    private int flag = 0;
    ActivityResultLauncher<Intent> mActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recog);

        captBtn = findViewById(R.id.capture_button);
        recogBtn = findViewById(R.id.detectText_button);
        imageView = findViewById(R.id.image_view);

        // REGISTERING RESULT ACTIVITY
        registerAct();

        // SCROLLABLE TEXT VIEW
        textView= findViewById(R.id.text_view);
        textView.setMovementMethod(new ScrollingMovementMethod());

        captBtn.setOnClickListener(view -> {
            checkCameraPermission();
            captureImg();
        });

        recogBtn.setOnClickListener(view -> detectText());
    }

    private void detectText() {
        if (flag==1) {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        StringBuilder resText = new StringBuilder();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            for (Text.Line line : block.getLines()) {
                                resText.append(line.getText());
                                textView.setText(resText);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to detect text..", Toast.LENGTH_LONG));
        }
        else {
            Toast.makeText(this,"Capture a image first..",Toast.LENGTH_LONG).show();
        }
    }


    private void captureImg() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mActivityResultLauncher.launch(intent);
    }

    private void registerAct() {
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent intent1 = result.getData();
                bitmap = (Bitmap) intent1.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                flag = 1;
            }
        });
    }


    // CHECKING PERMISSIONS/REQUESTING
    private void checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},IMAGE_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}