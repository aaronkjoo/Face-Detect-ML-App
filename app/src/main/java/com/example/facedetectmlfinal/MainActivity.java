package com.example.facedetectmlfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //UI Views
    private ImageView originalImageIv, croppedImageIv;
    private Button detectFacesBtn;

    // TAG for debugging
    private static final String TAG = "FACE_DETECT_TAG";

    // This factor is used to make detecting image smaller, to make the process faster
    private static final int SCALING_FACTOR = 10;
    private FaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalImageIv = findViewById(R.id.originalImageIv);
        croppedImageIv = findViewById(R.id.croppedImageIv);
        detectFacesBtn = findViewById(R.id.detectFacesBtn);

        FaceDetectorOptions realTimeFDO =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();

        detector = FaceDetection.getClient(realTimeFDO);
        //detector = (FaceDetector) FaceDetection.getClient(realTimeFDO);

        detectFacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bitmap from drawable
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.joo);
                // Bitmap from Uri, in case you want to detect face from image picked from gallery/camera
                /*
                Uri imageUri = null;
                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //Bitmap from ImageView, in case your image is in ImageView may be obtained from URL/web
                BitmapDrawable bitmapDrawable = (BitmapDrawable) originalImageIv.getDrawable();
                Bitmap bitmap1 = bitmapDrawable.getBitmap();*/
                analyzePhoto(bitmap);
            }
        });
    }
        private void analyzePhoto(Bitmap bitmap) {
            Log.d(TAG, "analyzePhoto: ");

            // Get smaller Bitmap to do analyze process faster
            Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    bitmap.getWidth() / SCALING_FACTOR,
                    bitmap.getHeight() / SCALING_FACTOR,
                    false);

            // Get input image using bitmap, you may use fromUri method
            InputImage inputImage = InputImage.fromBitmap(smallerBitmap, 0);

            // Start detection process
            detector.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                        @Override
                        public void onSuccess(List<Face> faces) {
                            // There can be multiple faces detected from an image, manage them using loop from List<Face> faces
                            Log.d(TAG, "onSuccess: No of faces detected: " + faces.size());
                            for (Face face : faces) {
                                // Get detected face as a rectangle
                                Rect rect = face.getBoundingBox();
                                rect.set(rect.left * SCALING_FACTOR,
                                        rect.top * (SCALING_FACTOR - 1),
                                        rect.right * SCALING_FACTOR,
                                        rect.bottom * SCALING_FACTOR + 90
                                );
                            }
                            cropDetectedFaces(bitmap, faces);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Detection failed
                            Log.e(TAG, "Onfailure: ", e);
                            Toast.makeText(MainActivity.this, "Detection failed due to : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            }


        private void cropDetectedFaces(Bitmap bitmap, List<Face> faces) {
            //Face was detected, now we will crop the face part of the image. There can be multiple faces, you can use loop to manage each.
            Rect rect = faces.get(0).getBoundingBox();

            int x = Math.max(rect.left, 0);
            int y = Math.max(rect.top, 0);
            int width = rect.width();
            int height = rect.height();

            Bitmap croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    x,
                    y,
                    (x + width > bitmap.getWidth()) ? bitmap.getWidth() - x : width,
                    (y + height > bitmap.getHeight()) ? bitmap.getHeight() - y : height
            );
            // set the cropped bitmap to imageview
            croppedImageIv.setImageBitmap(croppedBitmap);

        }

}