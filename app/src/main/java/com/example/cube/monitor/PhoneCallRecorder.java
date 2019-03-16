package com.example.cube.monitor;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by cube on 13/5/18.
 */

public class PhoneCallRecorder {

    boolean isRecordStarted = false;
    MediaRecorder recorder;
    AudioManager audioManager;
    Context context;

    PhoneCallRecorder(Context context ,MediaRecorder recorder) {
        this.context = context;
        this.recorder = recorder;
    }

     public void startMediaRecorder(/*final int audioSource*/){
        //turn on speaker
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        //increase Volume
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);


        recorder = new MediaRecorder();
        try{
            /*if (recorder != null) {
                recorder.release();
            }*/
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //recorder.setAudioSource(MediaRecorder.getAudioSourceMax());
            recorder.setAudioSamplingRate(8000);
            recorder.setAudioEncodingBitRate(12200);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()+
                    "/audio_record/" + System.currentTimeMillis() + "audio.amr";
            recorder.setOutputFile(fileName);

            MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
                public void onError(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e(TAG, "OnErrorListener " + arg1 + "," + arg2);
                    //terminateAndEraseFile();
                }
            };
            recorder.setOnErrorListener(errorListener);

            MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
                public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e(TAG, "OnInfoListener " + arg1 + "," + arg2);
                    //terminateAndEraseFile();
                }
            };
            recorder.setOnInfoListener(infoListener);


            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            isRecordStarted = true;
            //return true;
        }catch (RuntimeException e){
            e.getMessage();
            //return false;
        }catch (InterruptedException ef) {
            ef.printStackTrace();
        }catch (IOException dd) {
            dd.printStackTrace();
        }
    }

    public int getAudioSource(String str) {
        if (str.equals("MIC")) {
            return MediaRecorder.AudioSource.MIC;
        }
        else if (str.equals("VOICE_COMMUNICATION")) {
            return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        }
        else if (str.equals("VOICE_CALL")) {
            return MediaRecorder.AudioSource.VOICE_CALL;
        }
        else if (str.equals("VOICE_DOWNLINK")) {
            return MediaRecorder.AudioSource.VOICE_DOWNLINK;
        }
        else if (str.equals("VOICE_UPLINK")) {
            return MediaRecorder.AudioSource.VOICE_UPLINK;
        }
        else if (str.equals("VOICE_RECOGNITION")) {
            return MediaRecorder.AudioSource.VOICE_RECOGNITION;
        }
        else if (str.equals("CAMCORDER")) {
            return MediaRecorder.AudioSource.CAMCORDER;
        }
        else {
            return MediaRecorder.AudioSource.DEFAULT;
        }
    }

    public void stopRecording() {

        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecordStarted = false;

        }else if(recorder == null) {

            isRecordStarted = false;
            Toast.makeText(context,"No recording going on",Toast.LENGTH_SHORT).show();

        }
    }

}
