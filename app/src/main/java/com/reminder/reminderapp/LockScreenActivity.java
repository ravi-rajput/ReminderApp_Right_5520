package com.reminder.reminderapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.reminderapp.ActivityUtilsKt.turnScreenOffAndKeyguardOn;
import static com.example.reminderapp.ActivityUtilsKt.turnScreenOnAndKeyguardOff;

public class LockScreenActivity extends AppCompatActivity {
ImageView iv;
TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen_activity);
      iv=findViewById(R.id.iv);
      tv=findViewById(R.id.tv);
        Intent i=getIntent();
        tv.setText(i.getStringExtra("message"));
        Bitmap image = convert_to_bitmap(i.getStringExtra("image"));
        BitmapDrawable background = new BitmapDrawable(this.getResources(), image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            iv.setBackground(background);
        }
        turnScreenOnAndKeyguardOff(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnScreenOffAndKeyguardOn(this);
    }

    public Bitmap convert_to_bitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
