package com.nerya.vc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Reciever extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reciever);
        ImageView imageView = findViewById(R.id.imageView);

    }
    private void initForUser(String uuid,ImageView uimg){
        FirebaseDatabase dbU = FirebaseDatabase.getInstance();
        DatabaseReference dburf = dbU.getReference(uuid);
        DatabaseReference imgRef = dburf.child("image");
        imgRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Thread ta = new Thread() {
                    @Override
                    public void run() {
                        byte[] decodedString = Base64.decode(dataSnapshot.child("imagePixels").getValue().toString(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uimg.setImageBitmap(decodedByte);
                            }
                        });

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