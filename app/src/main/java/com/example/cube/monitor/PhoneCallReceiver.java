package com.example.cube.monitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.telephony.TelephonyManager;
import java.util.Date;
/**
 * Created by cube on 14/5/18.
 */

public class PhoneCallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    PhoneCallRecorder phoneCallRecorder;
    MediaRecorder recorder;
    TelephonyManager telephonyManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        recorder = new MediaRecorder();
        phoneCallRecorder = new PhoneCallRecorder(context,recorder);
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(telephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(telephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(telephonyManager.EXTRA_STATE_IDLE)){
                state = telephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(telephonyManager.EXTRA_STATE_OFFHOOK)){
                state = telephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(telephonyManager.EXTRA_STATE_RINGING)){
                state = telephonyManager.CALL_STATE_RINGING;
            }
            onCallStateChanged(context, state, number);

         }
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        phoneCallRecorder.startMediaRecorder();

        //super.onIncomingCallStarted(ctx, number, start);
    }


    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        phoneCallRecorder.startMediaRecorder();

        //super.onOutgoingCallStarted(ctx, number, start);
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        phoneCallRecorder.stopRecording();

        //super.onIncomingCallEnded(ctx, number, start, end);
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        phoneCallRecorder.stopRecording();

        //super.onOutgoingCallEnded(ctx, number, start, end);
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        //Do nothing
        //super.onMissedCall(ctx, number, start);
    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

}