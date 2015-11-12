package com.canonical.democlient;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class LedControlP extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Button led_back;
    private Switch redSwitch;
    private Switch greenSwitch;
    private Switch blueSwitch;

    private int red_status;
    private int green_status;
    private int blue_status;

    private boolean put_done = true;

    private String msg_type_done;

    public LedControlP(Activity a, String msg_type, int r, int g, int b) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        red_status = r;
        green_status = g;
        blue_status = b;
        msg_type_done = msg_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.led_control_p);

        redSwitch = (Switch) findViewById(R.id.switch_led_red);
        greenSwitch = (Switch) findViewById(R.id.switch_led_green);
        blueSwitch = (Switch) findViewById(R.id.switch_led_blue);

        redSwitch.setChecked(red_status != 0);
        greenSwitch.setChecked(green_status != 0);
        blueSwitch.setChecked(blue_status != 0);

        redSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    Log.e("LED Control P", "Red led on");
                    red_status = 1;
                } else {
                    Log.e("LED Control P", "Red led off");
                    red_status = 0;
                }
                disable_switch();
                sendMessage();
            }
        });

        greenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    Log.e("LED Control P", "Green led on");
                    green_status = 1;
                }else{
                    Log.e("LED Control P", "Green led off");
                    green_status = 0;
                }
                disable_switch();
                sendMessage();
            }
        });

        blueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    Log.e("LED Control P", "Blue led on");
                    blue_status = 1;
                }else{
                    Log.e("LED Control P", "Blue led off");
                    blue_status = 0;
                }
                disable_switch();
                sendMessage();
            }
        });


        led_back = (Button) findViewById(R.id.button_led_back);
        led_back.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this.c).registerReceiver(mLedPutDoneReceiver,
                new IntentFilter(msg_type_done));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_led_back:
                dismiss();
                break;
            default:
                break;
        }
    }

    private void disable_switch() {
        redSwitch.setEnabled(false);
        greenSwitch.setEnabled(false);
        blueSwitch.setEnabled(false);
    }

    private void enable_switch() {
        redSwitch.setEnabled(true);
        greenSwitch.setEnabled(true);
        blueSwitch.setEnabled(true);
    }

    private void sendMessage() {
        Intent intent = new Intent("msg_led_p_adjust");
        // You can also include some extra data.
        intent.putExtra("red", red_status);
        intent.putExtra("green", green_status);
        intent.putExtra("blue", blue_status);
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }

    private BroadcastReceiver mLedPutDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            put_done = intent.getBooleanExtra("put_done", false);
            if(put_done) {
                enable_switch();
            }
        }
    };
}
