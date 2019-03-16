package com.example.cube.monitor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private RelativeLayout relativeLayout1, relative_layout2;
    private ToggleButton toggleButton1, toggleButton2;
    private Button send_email;
    private static final int REQUEST_CODE_CAPTURE_PERM = 1234;
    //private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSION_KEY = 1;
    boolean isRecording = false;

    Context context = this;
    PhoneCallRecorder phoneCallRecorder;
    MediaRecorder recorder;


    private Intent voiceCallService;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private long checkAvailableSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = stat.getBlockCountLong() * stat.getAvailableBlocksLong();
        }
        return bytesAvailable / (1024 * 1024);     //megAvailable
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        boolean state = (buttonView).isChecked();
        switch (id) {
            case R.id.toggle_button_video:
                if (state == true) {
                    /*RecordingVideo(true);*/
                    onToggleScreenShare();

                } else if (state == false) {
                    /*RecordingVideo(false);*/
                    onToggleScreenShare();
                }

                break;
            case R.id.toggle_button_voice_calls:
                if (state == false) {
                    RecordingVoiceCalls(true);
                } else if (state == true) {
                    RecordingVoiceCalls(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        phoneCallRecorder = new PhoneCallRecorder(context, recorder);

        relativeLayout1 = findViewById(R.id.relative_layout1);
        toggleButton1 = relativeLayout1.findViewById(R.id.toggle_button_video);
        relative_layout2 = findViewById(R.id.relative_layout2);
        toggleButton2 = relative_layout2.findViewById(R.id.toggle_button_voice_calls);
        toggleButton1.setChecked(true);
        toggleButton2.setChecked(true);
        send_email = findViewById(R.id.send_email);

        String mediaState = Environment.getExternalStorageState();
        if (mediaState == null || !(mediaState.equals(Environment.MEDIA_MOUNTED)
                || mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("External media is unavailable (not mounted?). Please check.");
            AlertDialog dialog = builder.create();
            dialog.setButton(0, "OK and Restart",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }
            );
            dialog.setTitle("We have some Problem!");
            dialog.show();
            return;
        }

        long availableSpace = checkAvailableSpace();
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "screen_record");
        File folder1 = new File(Environment.getExternalStorageDirectory() +
                File.separator + "audio_record");
        boolean success = true, success1 = true;
        if (!folder.exists() || !folder1.exists()) {
            success = folder.mkdirs();
            success1 = folder1.mkdirs();
        }
        if (success && success1) {
            // Do something on success
            if (availableSpace > 200) {
                toggleButton1.setOnCheckedChangeListener(this);
                toggleButton2.setOnCheckedChangeListener(this);
            } else {
                Toast.makeText(this, "Your device is not having enough space", Toast.LENGTH_LONG).show();
                toggleButton1.setChecked(true);
                toggleButton2.setChecked(true);
            }
        } else {
            Toast.makeText(this, "Not able to make directory", Toast.LENGTH_LONG).show();
            // Do something else on failure
        }

        send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Send Email button clicked", Toast.LENGTH_LONG).show();
            }
        });


        startService(new Intent(this, EmailService.class));

        /*PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, StartActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        startService(new Intent(this, StartService.class));
        startService(new Intent(this, SmsOutgoingService.class));
        try {
            // Initiate DevicePolicyManager.
            DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName mAdminName = new ComponentName(this, DeviceAdminReciever.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                mDPM.lockNow();
                finish();
        //                 Intent intent = new Intent(MainActivity.this,
        //                 TrackDeviceService.class);
        //                 startService(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (REQUEST_CODE == requestCode) {
                    startService(new Intent(StartActivity.this, TService.class));
                    finish();
                }
                super.onActivityResult(requestCode, resultCode, data);
            }
        }*/

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            isRecording = false;
            toggleButton1.setChecked(true);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    //onToggleScreenShare();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Confirm Delete...");
                    alertDialog.setMessage("Please enable Microphone and Storage permissions.");
                    alertDialog.setPositiveButton("YES,Allow", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(intent);
                        }
                    });

                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
            }
            return;
        }
    }

    private void onToggleScreenShare() {
        if (!isRecording) {
            //Toast.makeText(this, "first button and state is start", Toast.LENGTH_SHORT).show();
            initRecorder();
            shareScreen();
        } else {        //if recording then stops it
            //Toast.makeText(this, "first button and state is stopped", Toast.LENGTH_SHORT).show();
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            stopScreenSharing();
        }
    }

    private void initRecorder() {
        try {
            //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //THREE_GPP
            mMediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/screen_record/" + System.currentTimeMillis() + "video.mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(256 * 1000);
            mMediaRecorder.setVideoFrameRate(20); // 30
            mMediaRecorder.setVideoEncodingBitRate(3000000);
            mMediaRecorder.setMaxFileSize(2000000L);    //i.e 200mb1024l = 1kb 2537253l =2.3mb
            mMediaRecorder.setMaxDuration(1800000);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);

    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        destroyMediaProjection();
        isRecording = false;
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording = false;
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @Override
    public void onBackPressed() {
        /*if (isRecording) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Delete...");
            alertDialog.setMessage("Do you want to cancel the recording");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    stopScreenSharing();
                    finish();
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });            alertDialog.show();
        } else {
            finish();
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    protected void onPause() {
        super.onPause();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void RecordingVideo(boolean start) {
        if (start) {
            Toast.makeText(this, "first button and state is start", Toast.LENGTH_SHORT).show();
            onToggleScreenShare();

        } else {
            Toast.makeText(this, "first button and state is stopped", Toast.LENGTH_SHORT).show();
            onToggleScreenShare();
        }
    }

    private void RecordingVoiceCalls(boolean startOrStop) {
        if (startOrStop) {
            Toast.makeText(this, "Voice calls service is on", Toast.LENGTH_LONG).show();
            //phoneCallRecorder.startMediaRecorder();
            voiceCallService = new Intent(this,ServiceForPhoneCalls.class);
            startService(voiceCallService);

        } else {
            Toast.makeText(this, "Voice call service is stopped", Toast.LENGTH_LONG).show();
            //phoneCallRecorder.stopRecording();
            stopService(voiceCallService);
        }
    }

}