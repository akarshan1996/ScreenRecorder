package com.example.cube.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cube on 30/5/18.
 */

public class EmailService extends Service{

    private PendingIntent pendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Email Service started", Toast.LENGTH_SHORT).show();
        //Trigger email class
        Intent alarmIntent = new Intent(this,SendEmail.class);

        pendingIntent = PendingIntent.getBroadcast(this
                , 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.AM_PM,Calendar.AM);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 19);

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
        sendBroadcast(alarmIntent);

        //manager.cancel(mAlarmIntent);     //Cancel the alarm

        /*SimpleDateFormat df = new SimpleDateFormat("hh:mm");
        Date d1 = df.parse("23:30");
        Calendar c1 = GregorianCalendar.getInstance();
        c1.setTime(d1);
        System.out.println(c1.getTime());*/

        return super.onStartCommand(intent, flags, startId);
    }

    public class SendEmail extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Toast.makeText(getApplicationContext(), "Got broadcast", Toast.LENGTH_SHORT).show();

                ArrayList<String> filePaths_List = new ArrayList<>();
                String screen_recordings = Environment.getExternalStorageDirectory().toString()
                        + File.separatorChar  + "screen_record/";
                String voice_recordings = Environment.getExternalStorageDirectory().toString()
                        + File.separatorChar + "audio_record/";

                File screen_directory = new File(screen_recordings);
                File voice_directory = new File(voice_recordings);

                File[] screen_files = screen_directory.listFiles();
                File[] voice_files = voice_directory.listFiles();

                int max_length = Math.max(screen_files.length, voice_files.length);

                for (int i = 0; i < max_length ;i++) {
                    String screen_file_name = "", voice_file_name = "",
                            screen_filePathWithExternalPath = "", voice_filePathWithExternalPath = "";
                    if(i < screen_files.length) {
                        screen_file_name = screen_files[i].getName();
                        screen_filePathWithExternalPath = screen_recordings + screen_file_name ;
                        filePaths_List.add(screen_filePathWithExternalPath);

                    }
                    if(i < voice_files.length) {
                        voice_file_name = voice_files[i].getName();
                        voice_filePathWithExternalPath = voice_recordings + voice_file_name ;
                        filePaths_List.add(voice_filePathWithExternalPath);
                    }
                    Log.d("Files", "FileName:" + screen_filePathWithExternalPath + "   "+ voice_filePathWithExternalPath);
                }

                String [] filePaths = filePaths_List.toArray(new String[filePaths_List.size()]);
                //String filePaths[] = {externalStoragePathStr + "fadeintodarkness.mp3"};
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"akarshan1996@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Here's your stuff");
                ArrayList<Uri> uris = new ArrayList<>();
                for (String file : filePaths) {
                    File fileIn = new File(file);
                    Uri u = Uri.fromFile(fileIn);
                    uris.add(u);
                }
                Toast.makeText(getApplicationContext(), "Inside Email", Toast.LENGTH_SHORT).show();

                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(emailIntent, "Send attachment with what app?"));

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
