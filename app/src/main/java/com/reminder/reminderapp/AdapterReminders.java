package com.reminder.reminderapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class AdapterReminders extends RecyclerView.Adapter<AdapterReminders.MyViewHolder>{

    private List<Reminders> allReminders;
    private TextView message,time,voice_note,date;
    ImageView image_bg,play,delete;
    Context context;
    androidx.cardview.widget.CardView parent,card_iv;
    int j=0;
    public AdapterReminders(List<Reminders> allReminders , Context context) {
        this.allReminders = allReminders;
        this.context=context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reminder_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {

        final Reminders reminders = allReminders.get(i);

            message.setText(reminders.getMessage());

                String filename = reminders.getVoice_note().substring(reminders.getVoice_note().lastIndexOf('/')+1);
//                voice_note.setText(filename);
            int[] androidColors =context.getResources().getIntArray(R.array.androidcolors);
//            int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
//            parent.setCardBackgroundColor(randomAndroidColor);


            if(androidColors.length-1>=i) {
    parent.setCardBackgroundColor(androidColors[i]);
    card_iv.setCardBackgroundColor(androidColors[i]);
}else{
                if(j<=i){
    parent.setCardBackgroundColor(androidColors[j]);
    card_iv.setCardBackgroundColor(androidColors[j]);
    j++;
}else{
                    j=0;
                }
            }

            Bitmap image = convert_to_bitmap(reminders.getImage());
//
            String formateDate = new SimpleDateFormat("dd-MM-yyyy").format(reminders.getRemindDate());
            Log.v("output_date ",formateDate);
            date.setText(formateDate);

            String formatetime = new SimpleDateFormat("hh:mm a").format(reminders.getRemindDate());
            time.setText(formatetime);




            BitmapDrawable background = new BitmapDrawable(context.getResources(), image);
           if(reminders.getImage().equals("null")){
               image_bg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_camera));
           }else {
               image_bg.setBackground(background);
           }
            final MediaPlayer mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(reminders.getVoice_note());
                mPlayer.prepare();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
          play.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  mPlayer.start();
              }
          });

        AppDatabase appDatabase = AppDatabase.geAppdatabase(context.getApplicationContext());
        final RoomDAO roomDAO = appDatabase.getRoomDAO();
        final Reminders reminder = new Reminders();

    reminder.setId(reminders.getId());
    reminder.setMessage(reminders.getMessage());
    reminder.setRemindDate(reminders.getRemindDate());
    reminder.setImage(reminders.getImage());
    reminder.setVoice_note(reminders.getVoice_note());

         delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("are you sure you want to remove this item")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                roomDAO.Delete(reminder);

                                   allReminders.remove(myViewHolder.getAdapterPosition());
                                   notifyItemRemoved(myViewHolder.getAdapterPosition());
                                                          }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialogue = builder.create();
                alertDialogue.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return allReminders.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.textView1);
            time = itemView.findViewById(R.id.textView2);
            date = itemView.findViewById(R.id.date);
            voice_note=itemView.findViewById(R.id.voice_note);
            image_bg=itemView.findViewById(R.id.image_bg);
            play=itemView.findViewById(R.id.play);
            parent=itemView.findViewById(R.id.parent);
            card_iv=itemView.findViewById(R.id.card_iv);
            delete=itemView.findViewById(R.id.delete);
        }
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
