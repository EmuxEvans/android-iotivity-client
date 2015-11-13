package com.canonical.democlient;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;

import java.util.ArrayList;

/**
 * Created by gerald on 2015/11/10.
 */
public class BuzzerResourceP extends DemoResource implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener {

    private double mBuzzer;
    private int mBuzzerListIndex;
    private static final String BUZZER_KEY = "buzzer";
    public final static String buzzer_display = "(RaspberryPi2) Buzzer: ";

    public BuzzerResourceP(Activity main, Context c, ArrayList<String> list_item,
                           ArrayAdapter<String> list_adapter) {

        resource_type = "grovepi.buzzer";
        resource_uri = "/grovepi/buzzer";

        mBuzzer = 0.0;
        mBuzzerListIndex = -1;

        msg_content_found = "msg_found_buzzer_p";
        msg_type_put_done = "msg_put_done_buzzer_p";
    }

    public void setBuzzerIndex(int index) { mBuzzerListIndex = index; }
    public int getBuzzerIndex() { return mBuzzerListIndex; }

    protected void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mBuzzerListIndex, buzzer_display);
                main_list_adapter.notifyDataSetChanged();
            }
        });
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mBuzzer = rep.getValue(BUZZER_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(BUZZER_KEY, mBuzzer);
        return rep;
    }

    public void putRep(double buzzer) {
        mBuzzer = buzzer;
        putResourceRepresentation();
    }

    protected void sendPutCompleteMessage() {
        sendBroadcastMessage(msg_type_put_done, msg_content_put_done, true);
    }
}
