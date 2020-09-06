package com.nerya.vc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class OpeningActivity extends AppCompatActivity {

    public static String userId;
    public String USER_ID_STRING = "idStringUser";
    private TextView idDisplay;
    private EditText callerId;
    private FirebaseDatabase db;
    private String callFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        idDisplay = findViewById(R.id.idDisplay);

        if(hasUuid()){
            updateID(userId);
        }else{
            uuid(8);
        }
        callerId = findViewById(R.id.callerId);
        findViewById(R.id.callBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id= callerId.getText().toString();
                call(id);
            }
        });
        db = FirebaseDatabase.getInstance();
        db.getReference().child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    String value = snapshot.child("status").getValue().toString().trim();
                    if(value.equals("called")){
                        callFrom = snapshot.child("from").getValue().toString().trim();
                        findViewById(R.id.adPrompt).setVisibility(View.VISIBLE);
                    }else {
                        Log.i("hey","err");
                    }
                }else{
                    Log.i("hey","erra");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpeningActivity.this,MainActivity.class);
                intent.putExtra("idToCall", "cld");
                startActivity(intent);
            }
        });
        findViewById(R.id.decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.adPrompt).setVisibility(View.GONE);
                db.getReference().child(userId).child("status").setValue("idle");
                db.getReference().child(callFrom).child("status").setValue("idle");
            }
        });
    }
    private void call(String idToCall){
        Intent intent = new Intent(OpeningActivity.this,MainActivity.class);
        Log.i("try",idToCall);
        intent.putExtra("idToCall", idToCall);
        startActivity(intent);
    }
    private void updateID(String id){
        idDisplay.setText("Your ID is " + id);
    }
    private boolean hasUuid(){
        SharedPreferences sharedPref = OpeningActivity.this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "none";
        String gotUserId = sharedPref.getString(USER_ID_STRING, defaultValue);
        if(gotUserId != defaultValue){
            userId = gotUserId;
            return true;
        }else{
            return false;
        }
    }
    private void uuid(int length){
        String ret = "";
        for(int i = 0; i < length; i++){
            String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
            Random rnd = new Random();
            char c = chars.charAt(rnd.nextInt(chars.length()));
            ret += c;
        }
        userId = ret;
        db = FirebaseDatabase.getInstance();
        db.getReference().child(ret).child("status").setValue("idle");
        SharedPreferences sharedPref = OpeningActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_ID_STRING, ret);
        editor.apply();
        updateID(ret);
    }
}