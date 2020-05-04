package com.reminder.reminderapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import kotlin.jvm.internal.Intrinsics;

public class MainPage extends AppCompatActivity {

    private FloatingActionButton add;
    RelativeLayout alarm_lay,toolbar;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private AdapterReminders adapter;
    private List<Reminders> temp;
    private TextView empty,time_text,date,cancel,voice_rcd_txt;
    final String path="/sdcard/media/alarms/";
     MediaRecorder recorder;
     String voice_path;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    ImageView iv1;
    String imageStoragePath,encodedImage1;
    public static final int SELECT_FILE=11;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";
    public static final String IMAGE_EXTENSION = "jpg";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int BITMAP_SAMPLE_SIZE = 8;
    public static final String VIDEO_EXTENSION = "mp4";
    String date_time,currentDateTimeString;
    EditText message;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        checkAndRequestPermissions();
        encodedImage1="null";
        voice_path="recording not found";
        appDatabase = AppDatabase.geAppdatabase(MainPage.this);
        time_text=findViewById(R.id.time);
        date=findViewById(R.id.date);
        alarm_lay=findViewById(R.id.alarm_lay);
        toolbar=findViewById(R.id.add_layout);
        add = findViewById(R.id.floatingButton);
        empty = findViewById(R.id.empty);
        cancel=findViewById(R.id.cancel);
        message = findViewById(R.id.message);
        voice_rcd_txt=findViewById(R.id.voice_rcd_txt);
        Intent var10000 = this.getIntent();
        if (Intrinsics.areEqual(var10000 != null ? var10000.getAction() : null, "android.intent.action.SEND")) {
            alarm_lay.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.GONE);

            empty.setVisibility(View.GONE);
        }
        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String name = sharedpreferences.getString("is_first_time", "false");
        if(name.equals("false")){
            addAutoStartup();
        }
        currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm_lay.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

                empty.setVisibility(View.GONE);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm_lay.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                setItemsInRecyclerView();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainPage.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        setItemsInRecyclerView();

addReminder();
    }

    public void addReminder(){


        final ImageView startButton;
        final ImageView stopButton;

        iv1=findViewById(R.id.iv);
        LinearLayout select;
        select = findViewById(R.id.selectDate);
       TextView add = findViewById(R.id.addButton);



        Intent var10000 = this.getIntent();
        if (Intrinsics.areEqual(var10000 != null ? var10000.getAction() : null, "android.intent.action.SEND")) {

            Intent var10001 = this.getIntent();
            Intrinsics.checkExpressionValueIsNotNull(var10001, "intent");
            this.handleSendImage(var10001,iv1);

        }

        recorder = new MediaRecorder();


        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainPage.this);
                alertDialogBuilder.setMessage("Pick Image From");


                    alertDialogBuilder.setNegativeButton("Gallary",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);//
                            startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);

                        }
                    });
                    alertDialogBuilder.setPositiveButton("Camera",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (CameraUtils.checkPermissions(getApplicationContext())) {
                                        captureImage();
                                    } else {
                                        requestCameraPermission(MEDIA_TYPE_IMAGE);
                                    }

                                }
                            });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });


        stopButton.setVisibility(View.GONE);
        //make directory
        boolean exists = (new File(path)).exists();
        if (!exists){new File(path).mkdirs();}
startButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    startButton.setVisibility(View.GONE);
    stopButton.setVisibility(View.VISIBLE);


    recorder = new MediaRecorder();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    // recorder.setMaxDuration(50000); // 50 seconds
    recorder.setAudioEncodingBitRate(160 * 1024);
    //recorder.setAudioChannels(2);
    recorder.setOutputFile(path + currentDateTimeString + ".mp3");


    try {
        recorder.prepare();
        recorder.start();
    } catch (Exception e) {
        Toast.makeText(MainPage.this, e.toString(), Toast.LENGTH_SHORT).show();
        // TODO Auto-generated catch block
        e.printStackTrace();
    }


    }
});

stopButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
try {
    recorder.stop();
    recorder.reset();
    recorder.release();
    recorder = null;

    addRecordingToMediaLibrary(currentDateTimeString + ".mp3");
    Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
    voice_rcd_txt.setText("voice recorded successfully");
}catch (Exception ae){
    Toast.makeText(MainPage.this, ae.toString(), Toast.LENGTH_SHORT).show();
}
    }
});


        final Calendar newCalender = Calendar.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MainPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainPage.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year,month,dayOfMonth,hourOfDay,minute,0);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME", System.currentTimeMillis()+"");
                                if(newDate.getTimeInMillis()-tem.getTimeInMillis()>0) {

                                  date_time=newDate.getTime().toString();
                                    Log.v("output_date ", date_time);


                                    String formateDate = new SimpleDateFormat("dd-MM-yyyy").format(newDate.getTime());
                                    date.setText(formateDate);

                                    String formatetime = new SimpleDateFormat("hh:mm a").format(newDate.getTime());
                                    time_text.setText(formatetime);


                                }
                                else
                                    Toast.makeText(MainPage.this,"Invalid time", Toast.LENGTH_SHORT).show();

                            }
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE),false);
                        time.show();
                    }
                },newCalender.get(Calendar.YEAR),newCalender.get(Calendar.MONTH),newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(message.getText().toString().equals("")&&voice_path.equals("recording not found")
                &&encodedImage1.equals("null")){
                    Toast.makeText(MainPage.this, "Please Fill your note", Toast.LENGTH_SHORT).show();
                }else if(time_text.getText().toString().equals("")){
                    Toast.makeText(MainPage.this, "Please Select Date", Toast.LENGTH_SHORT).show();
                }else {

                    RoomDAO roomDAO = appDatabase.getRoomDAO();
                    Reminders reminders = new Reminders();
                    reminders.setMessage(message.getText().toString().trim());
                    Date remind = new Date(date_time);
                    reminders.setRemindDate(remind);
                    reminders.setVoice_note(voice_path);
                    reminders.setImage(encodedImage1);
                    roomDAO.Insert(reminders);
                    List<Reminders> l = roomDAO.getAll();
                    reminders = l.get(l.size() - 1);
                    Log.e("ID chahiye", reminders.getId() + "");

                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                    calendar.setTime(remind);
                    calendar.set(Calendar.SECOND, 0);
                    Intent intent = new Intent(MainPage.this, NotifierAlarm.class);
                    intent.putExtra("Message", reminders.getMessage());
                    intent.putExtra("RemindDate", reminders.getRemindDate().toString());
                    intent.putExtra("id", reminders.getId());
                    intent.putExtra("image",reminders.getImage());
                    Log.d("encoded_image",reminders.getImage());
                    PendingIntent intent1 = PendingIntent.getBroadcast(MainPage.this, reminders.getId(), intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);
                    }

                    Toast.makeText(MainPage.this, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                    setItemsInRecyclerView();
                    AppDatabase.destroyInstance();
                    alarm_lay.setVisibility(View.GONE);
                    toolbar.setVisibility(View.VISIBLE);
                    nullItems();
                }
            }
        });



    }
public  void nullItems(){
    iv1.setImageDrawable(null);
    encodedImage1="null";
    message.setText("");
    message.setHint("Type your message here");

    }
    public void setItemsInRecyclerView(){

        RoomDAO dao = appDatabase.getRoomDAO();
        temp = dao.orderThetable();
        if(temp.size()>0) {
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            empty.setVisibility(View.VISIBLE);
        }
        adapter = new AdapterReminders(temp,this);
        recyclerView.setAdapter(adapter);


    }

    protected void addRecordingToMediaLibrary(String audioname) {


        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.MediaColumns.DATA, path+audioname );
        values.put(MediaStore.MediaColumns.TITLE,  path+audioname );
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");

        //new
        values.put(MediaStore.Audio.Media.ARTIST, "cssounds ");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        ///new

        // values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
        // ContentResolver contentResolver = getContentResolver();
        //Insert it into the database
        this.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(path+audioname), values);
        //RingtoneManager.setActualDefaultRingtoneUri(Activity.this,
        //        RingtoneManager.TYPE_RINGTONE, newUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(new File(audioname)); // out is your output file
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } else {

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+path+audioname
                    + Environment.getExternalStorageDirectory())));
        }

voice_path="file://"+path+audioname;
Log.d("Path_audio","file://"+path+audioname
        + Environment.getExternalStorageDirectory());
        // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+path+editText1)));
        //Toast.makeText(this, "New Alarm Sound " + editText1 +format, Toast.LENGTH_LONG).show();
    }

    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
//                                    captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(MainPage.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    private String getBase64String(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }

    private String getBase64Stringgallary(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }
    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == SELECT_FILE) {
            Bitmap bm = null;
            if (data != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                iv1.setImageBitmap(bm);
                encodedImage1 = getBase64Stringgallary(bm);
            }

        } else if (resultCode == RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getApplicationContext(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        }


    }


    private final void handleSendImage(Intent intent, ImageView imageView) {
        Parcelable var10000 = intent.getParcelableExtra("android.intent.extra.STREAM");
        if (!(var10000 instanceof Uri)) {
            var10000 = null;
        }

        Uri var8 = (Uri)var10000;
        if (var8 != null) {

                Uri var2 = var8;
                try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), var2);
                encodedImage1=getBase64String(bitmap);

            }
            catch (Exception e)
            {
                //handle exception
            }
            imageView.setImageURI(var2);


        }

    }



    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }
    /**
     * Display image from gallery
     */
    private void previewCapturedImage() {
        try {

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            File f = new File(imageStoragePath);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(f.getPath());
            } catch (IOException e) {
                Toast.makeText(this, "No", Toast.LENGTH_SHORT).show();
            }
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), mat, true);
            Bitmap.Config config = rotatedBitmap.getConfig();


            Bitmap result = Bitmap.createBitmap( rotatedBitmap.getWidth(), rotatedBitmap.getHeight(),config);

            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(rotatedBitmap, 0, 0, null);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);

//    paint.setStyle(Paint.Style.STROKE);
            paint.setTextSize(15);
            paint.setAntiAlias(true);

            Paint innerPaint = new Paint();
            innerPaint.setColor(Color.parseColor("#61ECECEC"));
//    innerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            innerPaint.setAntiAlias(true);
            canvas.drawRect(180F, 50F, 0, 0, innerPaint);
            canvas.drawText("Click By Reminder App",5, 45, paint);
                iv1.setImageBitmap(result);
                encodedImage1= getBase64String(result);

        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }}

    public boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int loc2 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void addAutoStartup() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("is_first_time", "true");
        editor.commit();
        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("exc" , String.valueOf(e));
        }
    }

}
