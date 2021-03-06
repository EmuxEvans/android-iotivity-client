package com.canonical.democlient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

import java.util.ArrayList;

/**
 * Created by gerald on 2015/11/10.
 */
public class SensorResourceA extends DemoResource implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener {

    private double mTemp;
    private int mLight;
    private int mSound;
    private int mTempListIndex;
    private int mLightListIndex;
    private int mSoundListIndex;
    private static final String SENSOR_TEMPERATURE_KEY = "temperature";
    private static final String SENSOR_LIGHT_KEY = "light";
    private static final String SENSOR_SOUND_KEY = "sound";
    private final static String sensor_temp_display = "(Arduino) Temperature sensor: ";
    private final static String sensor_light_display = "(Arduino) Light sensor: ";
    private final static String sensor_sound_display = "(Arduino) Sound sensor: ";

    private boolean update_thread_done;

    public SensorResourceA(Activity main, Context c, ArrayList<String> list_item,
                           ArrayAdapter<String> list_adapter) {

        super(main, c, list_item, list_adapter);

        resource_type = "gateway.sensora";
        resource_uri = "/gateway/sensora";

        mTemp = 0.0;
        mLight = 0;
        mSound = 0;

        mTempListIndex = -1;
        mLightListIndex = -1;
        mSoundListIndex = -1;

        msg_content_found = "msg_found_sensor_a";

        msg_type_get_done = "msg_get_done_sensor_a";
        update_thread_done = true;

        LocalBroadcastManager.getInstance(main_activity).registerReceiver(mSensorReadReceiver,
                new IntentFilter(msg_type_get_done));
    }

    protected void update_thread() {
        Log.e(TAG, "Start update thread");
        while(update_thread_running) {
            Log.e(TAG, "Start update sensors: " + String.valueOf(update_thread_done));
            if(update_thread_done) {
                update_thread_done = false;
                getResourceRepresentation();
                try {
                    update_thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }

            try {
                update_thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
    }

    public void setTempIndex(int index) { mTempListIndex = index; }
    public void setLightIndex(int index) { mLightListIndex = index; }
    public void setSoundIndex(int index) { mSoundListIndex = index; }

    public int getTempIndex() { return mTempListIndex; }
    public int getLightIndex() { return mLightListIndex; }
    public int getSoundIndex() { return mSoundListIndex; }

    protected void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mTempListIndex, sensor_temp_display + String.valueOf(mTemp));
                main_list_item.set(mLightListIndex, sensor_light_display + String.valueOf(mLight));
                main_list_item.set(mSoundListIndex, sensor_sound_display + String.valueOf(mSound));
                main_list_adapter.notifyDataSetChanged();

                Log.e(TAG, "Arduino Sensors:");
                Log.e(TAG, String.valueOf(mTemp));
                Log.e(TAG, String.valueOf(mLight));
                Log.e(TAG, String.valueOf(mSound));
            }
        });
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mTemp = rep.getValue(SENSOR_TEMPERATURE_KEY);
        mLight = rep.getValue(SENSOR_LIGHT_KEY);
        mSound = rep.getValue(SENSOR_SOUND_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(SENSOR_TEMPERATURE_KEY, mTemp);
        rep.setValue(SENSOR_LIGHT_KEY, mLight);
        rep.setValue(SENSOR_SOUND_KEY, mSound);
        return rep;
    }

    protected void sendGetCompleteMessage() {
        sendBroadcastMessage(msg_type_get_done, msg_content_get_done, true);
    }

    private BroadcastReceiver mSensorReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            update_thread_done = intent.getBooleanExtra(msg_content_get_done, false);
        }
    };
}
