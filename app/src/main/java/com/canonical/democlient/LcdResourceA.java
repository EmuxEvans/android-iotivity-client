package com.canonical.democlient;

import android.app.Activity;
import android.content.Context;
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
public class LcdResourceA extends DemoResource implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener {

    private String mLcd;
    private int mLcdListIndex;
    private static final String LCD_STRING_KEY = "lcd";
    public final static String lcd_display = "(Arduino) LCD: ";

    public LcdResourceA(Activity main, Context c, ArrayList<String> list_item,
                        ArrayAdapter<String> list_adapter) {

        resource_type = "grove.lcd";
        resource_uri = "/grove/lcd";

        mLcd = "";
        mLcdListIndex = -1;

        msg_content_found = "msg_found_lcd_a";
    }

    public void setLcdIndex(int index) { mLcdListIndex = index; }
    public int getLcdIndex() { return mLcdListIndex; }

    protected void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mLcdListIndex, lcd_display + mLcd);
                main_list_adapter.notifyDataSetChanged();
                Log.e(TAG, "Arduino LCD:");
                Log.e(TAG, mLcd);
            }
        });
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mLcd = rep.getValue(LCD_STRING_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(LCD_STRING_KEY, mLcd);
        return rep;
    }

    public void putRep(String str) {
        mLcd = str;
        putResourceRepresentation();
    }
}
