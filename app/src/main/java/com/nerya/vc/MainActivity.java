package com.nerya.vc;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    PreviewView mPreviewView;
    private FirebaseDatabase db;
    private DatabaseReference dbRf,usrDbRf;
    private String callUid;
    private String uid = OpeningActivity.userId;
    private Bitmap bitmapOfCam;
    private ImageView callerImg;
    //FTlksbjvVA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callUid = getIntent().getStringExtra("idToCall");
        mPreviewView = findViewById(R.id.camera);
        callerImg = findViewById(R.id.callerImg);
        db = FirebaseDatabase.getInstance();
        dbRf = db.getReference(uid);
        usrDbRf = db.getReference(callUid);
        if(!callUid.equals("cld")){
            callUser(callUid);
        }else{
            db.getReference(uid).child("from").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    periodicUpdate.run();
                    initForUser(snapshot.getValue().toString(),callerImg);
                    db.getReference().child(callUid).child("status").setValue("talking");
                    db.getReference().child(uid).child("status").setValue("talking");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        //captureImage = findViewById(R.id.captureImg);
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        db.getReference(callUid).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString().equals("talking")){
                    periodicUpdate.run();
                    initForUser(callUid,callerImg);
                }else {
                    Log.i("heyy",snapshot.getValue().toString());
                    Log.i("heyy",snapshot.getValue().toString().equals("idle") + "");
                    if(snapshot.getValue().toString().equals("idle")) {
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void callUser(String user){
        DatabaseReference myRef;
        myRef = db.getReference(user).child("status");
        myRef.setValue("called");
        myRef = db.getReference(user).child("from");
        myRef.setValue(uid);
        myRef = db.getReference(uid).child("status");
        myRef.setValue("calling");
    }
    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable () {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run() {
            Log.i("ggg","gggggg");
            if(mPreviewView.getBitmap() != null){
                db.getReference().child(callUid).child("status").setValue("talking");
                db.getReference().child(uid).child("status").setValue("talking");
                Thread t = new Thread() {
                    @Override
                    public void run() {

                        Bitmap bitty = mPreviewView.getBitmap();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitty.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        DatabaseReference myRef;
                        myRef = db.getReference(uid).child("image").child("imagePixels");
                        myRef.setValue(encodedImage);
                    }
                };
                t.start();
            }else{
                Log.i("aaa","GGG");
            }
            handler.postDelayed(periodicUpdate, 300);
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                startCamera();
            }
        }
    }
    private Executor executor = Executors.newSingleThreadExecutor();
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                    Log.i("heyThere",mPreviewView.getBitmap() + "");
                    bitmapOfCam = mPreviewView.getBitmap();

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);
    }
    private void initForUser(String uuid,ImageView uimg){
        FirebaseDatabase dbU = FirebaseDatabase.getInstance();
        DatabaseReference dburf = dbU.getReference(uuid);
        DatabaseReference imgRef = dburf.child("image");
        dbU.getReference(uuid).child("image").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Thread ta = new Thread() {
                    @Override
                    public void run() {
                        try {
                            byte[] decodedString = Base64.decode(dataSnapshot.child("imagePixels").getValue().toString(), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    uimg.setImageBitmap(decodedByte);
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }
                };
                ta.start();
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}