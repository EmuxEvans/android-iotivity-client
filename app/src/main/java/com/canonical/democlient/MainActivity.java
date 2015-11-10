package com.canonical.democlient;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.iotivity.base.ErrorCode;
import org.iotivity.base.ModeType;
import org.iotivity.base.ObserveType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResourceIdentifier;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class MainActivity extends AppCompatActivity{
    private final static String TAG = MainActivity.class.getSimpleName();

    private SensorResourceA sensor_a = null;
    private LedResourceA led_a = null;
    private LcdResourceA lcd_a = null;
    private BuzzerResourceA buzzer_a = null;
    private ButtonResourceA button_a = null;


    /* Items in ListView */
    private ArrayList<String> list_item;
    private ArrayAdapter<String> list_adapter;
    private int found_devices = 0;
    private final Semaphore list_lock = new Semaphore(1, true);



    private void startDemoClient() {
        Context context = this;

        PlatformConfig platformConfig = new PlatformConfig(
                context,
                ServiceType.IN_PROC,
                ModeType.CLIENT,
                "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
                0,         // Uses randomly available port
                QualityOfService.LOW
        );

        msg("Configuring platform.");
        OcPlatform.Configure(platformConfig);

        msg("Finding all resources");
        sensor_a = new SensorResourceA(MainActivity.this, this, list_item, list_adapter);
        sensor_a.find_resource();

        led_a = new LedResourceA(MainActivity.this, this, list_item, list_adapter);
        led_a.find_resource();

        lcd_a = new LcdResourceA(MainActivity.this, this, list_item, list_adapter);
        lcd_a.find_resource();

        buzzer_a = new BuzzerResourceA(MainActivity.this, this, list_item, list_adapter);
        buzzer_a.find_resource();

        button_a = new ButtonResourceA(MainActivity.this, this, list_item, list_adapter);
        button_a.find_resource();
    }

    private void add_device() {
        if(found_devices != 0) {
            list_item.add("");
        }
    }

    private void create_list(final String device) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                /*
                try {
                    list_lock.acquire();
                } catch (InterruptedException e) {
                    msg("list_lock error");
                }
                */

                if(device.equals("sensor_found_resource")){
                    add_device();
                    sensor_a.setTempIndex(found_devices++);
                    add_device();
                    sensor_a.setLightIndex(found_devices++);
                    add_device();
                    sensor_a.setSoundIndex(found_devices++);
                    list_adapter.notifyDataSetChanged();
                } else if(device.equals("led_found_resource")) {
                    add_device();
                    led_a.setLedIndex(found_devices++);
                    list_item.set(led_a.getLedIndex(), led_a.led_display);
                    list_adapter.notifyDataSetChanged();
                } else if(device.equals("lcd_found_resource")) {
                    add_device();
                    lcd_a.setLcdIndex(found_devices++);
                    list_item.set(lcd_a.getLcdIndex(), lcd_a.lcd_display);
                    list_adapter.notifyDataSetChanged();
                } else if(device.equals("buzzer_found_resource")) {
                    add_device();
                    buzzer_a.setBuzzerIndex(found_devices++);
                    list_item.set(buzzer_a.getBuzzerIndex(), buzzer_a.buzzer_display);
                    list_adapter.notifyDataSetChanged();
                } else if(device.equals("button_found_resource")) {
                    add_device();
                    button_a.setButtonIndex(found_devices++);
                    list_item.set(button_a.getButtonIndex(), button_a.button_display);
                    add_device();
                    button_a.setTouchIndex(found_devices++);
                    list_item.set(button_a.getTouchIndex(), button_a.button_touch_display);
                    list_adapter.notifyDataSetChanged();
                }

                //list_lock.release();
            }
        });
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    private void msg(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                mConsoleTextView.append("\n");
                mConsoleTextView.append(text);
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        Log.i(TAG, text);
    }

    private void printLine() {
        msg("------------------------------------------------------------------------");
    }

    private synchronized void resetGlobals() {
        sensor_a.reset();
        led_a.reset();
        lcd_a.reset();
        buzzer_a.reset();
        button_a.reset();
    }

    private TextView mConsoleTextView;
    private ScrollView mScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConsoleTextView = (TextView) findViewById(R.id.consoleTextView);
        mConsoleTextView.setMovementMethod(new ScrollingMovementMethod());
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollView.fullScroll(View.FOCUS_DOWN);
        final Button button = (Button) findViewById(R.id.button_findserver);

        ListView listview = (ListView) findViewById(R.id.listView);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Log.e(TAG, String.valueOf(id));
                if (id < found_devices) {
                    if (id == led_a.getLedIndex()) {
                        LedControl led_dialog = new LedControl(MainActivity.this,
                                led_a.msg_put_done, led_a.getLed());
                        led_dialog.show();
                    } else if (id == lcd_a.getLcdIndex()) {
                        LcdControl lcd_dialog = new LcdControl(MainActivity.this);
                        lcd_dialog.show();
                    } else if (id == buzzer_a.getBuzzerIndex()) {
                        BuzzerControl buzzer_dialog = new BuzzerControl(MainActivity.this,
                                buzzer_a.msg_put_done);
                        buzzer_dialog.show();
                    }
                } else {
                    msg("Out of range");
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mFoundResourceReceiver,
                new IntentFilter("msg_found_a"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mLedControlReceiver,
                new IntentFilter("msg_led_adjust"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mLcdControlReceiver,
                new IntentFilter("msg_lcd_string"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mBuzzerControlReceiver,
                new IntentFilter("msg_buzzer_tone"));

        list_item = new ArrayList<String>();
        list_item.add("");

        list_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list_item);
        listview.setAdapter(list_adapter);


        if (null == savedInstanceState) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setText("Re-start");
                    button.setEnabled(false);
                    new Thread(new Runnable() {
                        public void run() {
                            startDemoClient();
                        }
                    }).start();
                }
            });
        } else {
            String consoleOutput = savedInstanceState.getString("consoleOutputString");
            mConsoleTextView.setText(consoleOutput);
        }

    }

    private BroadcastReceiver mFoundResourceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean found_resource;

            found_resource = intent.getBooleanExtra("sensor_found_resource", false);
            if(found_resource) {
                msg("Message: found sensor resource");
                create_list("sensor_found_resource");
                //sensor_a.getResourceRepresentation();
                sensor_a.start_update_thread();
                return;
            }

            found_resource = intent.getBooleanExtra("led_found_resource", false);
            if(found_resource) {
                msg("Message: found LED resource");
                create_list("led_found_resource");
                led_a.getResourceRepresentation();
                return;
            }

            found_resource = intent.getBooleanExtra("lcd_found_resource", false);
            if(found_resource) {
                msg("Message: found LCD resource");
                create_list("lcd_found_resource");
                lcd_a.getResourceRepresentation();
                return;
            }

            found_resource = intent.getBooleanExtra("buzzer_found_resource", false);
            if(found_resource) {
                msg("Message: found buzzer resource");
                create_list("buzzer_found_resource");
                buzzer_a.getResourceRepresentation();
                return;
            }

            found_resource = intent.getBooleanExtra("button_found_resource", false);
            if(found_resource) {
                msg("Message: found button resource");
                create_list("button_found_resource");
                //button_a.getResourceRepresentation();
                button_a.observeFoundResource();
                return;
            }

        }
    };

    private BroadcastReceiver mLedControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int progress = intent.getIntExtra("progress", 0);
            msg("LED status: " + String.valueOf(progress));
            led_a.putResourceRepresentation(progress);
        }
    };

    private BroadcastReceiver mLcdControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String str = intent.getStringExtra("string");
            msg("LCD string: " + str);
            lcd_a.putResourceRepresentation(str);
        }
    };

    private BroadcastReceiver mBuzzerControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int tone = intent.getIntExtra("tone", 0);
            msg("Buzzer tone: " + String.valueOf(tone));
            buzzer_a.putResourceRepresentation(tone);
        }
    };
}
