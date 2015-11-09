package com.canonical.democlient;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class MainActivity extends AppCompatActivity implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener{

    private Map<OcResourceIdentifier, OcResource> mFoundResources = new HashMap<>();
    private OcResource mSensorResourceA = null;
    private OcResource mLedResourceA = null;
    private OcResource mLcdResourceA = null;
    private OcResource mBuzzerResourceA = null;
    private OcResource mButtonResourceA = null;
    private DemoResource mDemo = new DemoResource();

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String sensor_name_temp = "(Arduino) Temperature sensor: ";
    private final static String sensor_name_light = "(Arduino) Light sensor: ";
    private final static String sensor_name_sound = "(Arduino) Sound sensor: ";

    private final static String led_name = "(Arduino) LED: ";
    private final static String lcd_name = "(Arduino) LCD: ";
    private final static String buzzer_name = "(Arduino) Buzzer: ";
    private final static String button_name_button = "(Arduino) Button: ";
    private final static String button_name_touch = "(Arduino) Touch: ";

    ArrayList<String> list_item;
    ArrayAdapter<String> list_adapter;
    private int found_devices = 0;

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

        try {
            String requestUri;

            msg("Finding resources of type \"grove.sensor\".");
            requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.sensor";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

            msg("Finding resources of type \"grove.led\".");
            requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.led";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

            msg("Finding resources of type \"grove.lcd\".");
            requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.lcd";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

            msg("Finding resources of type \"grove.buzzer\".");
            requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.buzzer";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

            msg("Finding resources of type \"grove.button\".");
            requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=grove.button";
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );

        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to invoke find resource API");
        }

        printLine();
    }

    private void add_device() {
        if(found_devices != 0) {
            list_item.add("");
        }
    }

    private void create_list_sensor() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                add_device();
                mDemo.setTempIndex(found_devices++);
                add_device();
                mDemo.setLightIndex(found_devices++);
                add_device();
                mDemo.setSoundIndex(found_devices++);
                list_adapter.notifyDataSetChanged();
            }
        });

    }

    private void update_list_sensor(final OcRepresentation rep) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                try {
                    mDemo.sensorSetOcRepresentation(rep);
                    list_item.set(mDemo.getTempIndex(), sensor_name_temp + String.valueOf(mDemo.getTemp()));
                    list_item.set(mDemo.getLightIndex(), sensor_name_light + String.valueOf(mDemo.getLight()));
                    list_item.set(mDemo.getSoundIndex(), sensor_name_sound + String.valueOf(mDemo.getSound()));
                    list_adapter.notifyDataSetChanged();

                    msg("Sensors:");
                    msg(String.valueOf(mDemo.getTemp()));
                    msg(String.valueOf(mDemo.getLight()));
                    msg(String.valueOf(mDemo.getSound()));
                } catch (OcException e) {
                    Log.e(TAG, e.toString());
                    msg("Failed to read the attributes of a sensor resource");
                }
            }
        });
    }

    private void create_list_led() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                add_device();
                mDemo.setLedIndex(found_devices++);
                list_item.set(mDemo.getLedIndex(), led_name);
                list_adapter.notifyDataSetChanged();
            }
        });

    }

    private void update_list_led(final OcRepresentation rep) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                try {
                    mDemo.ledSetOcRepresentation(rep);
                    list_item.set(mDemo.getLedIndex(), led_name + String.valueOf(mDemo.getLed()));
                    list_adapter.notifyDataSetChanged();

                    msg("LED:");
                    msg(String.valueOf(mDemo.getLed()));
                } catch (OcException e) {
                    Log.e(TAG, e.toString());
                    msg("Failed to read the attributes of a LED resource");
                }
            }
        });
    }

    private void create_list_lcd() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                add_device();
                mDemo.setLcdIndex(found_devices++);
                list_item.set(mDemo.getLcdIndex(), lcd_name);
                list_adapter.notifyDataSetChanged();
            }
        });

    }

    private void update_list_lcd(final OcRepresentation rep) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                try {
                    mDemo.lcdSetOcRepresentation(rep);
                    list_item.set(mDemo.getLcdIndex(), lcd_name + mDemo.getLcd());
                    list_adapter.notifyDataSetChanged();

                    msg("LCD:");
                    msg(mDemo.getLcd());
                } catch (OcException e) {
                    Log.e(TAG, e.toString());
                    msg("Failed to read the attributes of a LCD resource");
                }

            }
        });
    }

    private void create_list_buzzer() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                add_device();
                mDemo.setBuzzerIndex(found_devices++);
                list_item.set(mDemo.getBuzzerIndex(), buzzer_name);
                list_adapter.notifyDataSetChanged();
            }
        });

    }

    private void create_list_button() {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                add_device();
                mDemo.setButtonIndex(found_devices++);
                list_item.set(mDemo.getButtonIndex(), button_name_button);
                add_device();
                mDemo.setTouchIndex(found_devices++);
                list_item.set(mDemo.getTouchIndex(), button_name_touch);
                list_adapter.notifyDataSetChanged();
            }
        });

    }

    private void update_list_button(final OcRepresentation rep) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                try {
                    //Read attribute values into local representation of a LED
                    mDemo.buttonSetOcRepresentation(rep);
                    list_item.set(mDemo.getButtonIndex(), button_name_button + String.valueOf(mDemo.getButton()));
                    list_item.set(mDemo.getTouchIndex(), button_name_touch + String.valueOf(mDemo.getTouch()));
                    list_adapter.notifyDataSetChanged();

                    msg("Button:");
                    msg(String.valueOf(mDemo.getButton()));
                    msg(String.valueOf(mDemo.getTouch()));
                } catch (OcException e) {
                    Log.e(TAG, e.toString());
                    msg("Failed to read the attributes of a button resource");
                }

            }
        });
    }

    private void update_sensor_thread() {
        while(true) {
            getSensorResourceRepresentation();
            sleep(3);
        }
    }

    /**
     * An event handler to be executed whenever a "findResource" request completes successfully
     *
     * @param ocResource found resource
     */
    @Override
    public synchronized void onResourceFound(OcResource ocResource) {
        if (null == ocResource) {
            msg("Found resource is invalid");
            return;
        }

        if (mFoundResources.containsKey(ocResource.getUniqueIdentifier())) {
            msg("Found a previously seen resource again!");
        } else {
            msg("Found resource for the first time on server with ID: " + ocResource.getServerId());
            mFoundResources.put(ocResource.getUniqueIdentifier(), ocResource);
        }

        // Get the resource URI
        String resourceUri = ocResource.getUri();
        // Get the resource host address
        String hostAddress = ocResource.getHost();
        msg("\tURI of the resource: " + resourceUri);
        msg("\tHost address of the resource: " + hostAddress);
        // Get the resource types
        msg("\tList of resource types: ");
        for (String resourceType : ocResource.getResourceTypes()) {
            msg("\t\t" + resourceType);
        }
        msg("\tList of resource interfaces:");
        for (String resourceInterface : ocResource.getResourceInterfaces()) {
            msg("\t\t" + resourceInterface);
        }
        msg("\tList of resource connectivity types:");
        for (OcConnectivityType connectivityType : ocResource.getConnectivityTypeSet()) {
            msg("\t\t" + connectivityType);
        }
        printLine();

        //In this example we are only interested in the light resources
        if (resourceUri.equals("/grove/sensor")) {
            if (mSensorResourceA != null) {
                msg("Found another sensor resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mSensorResourceA = ocResource;

            create_list_sensor();

            // Call a local method which will internally invoke "get" API on the SensorResource

            new Thread(new Runnable() {
                public void run() {
                    update_sensor_thread();
                }
            }).start();

            //getSensorResourceRepresentation();
        }

        if (resourceUri.equals("/grove/led")) {
            if (mLedResourceA != null) {
                msg("Found another LED resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mLedResourceA = ocResource;

            create_list_led();

            // Call a local method which will internally invoke "get" API on the SensorResource
            getLedResourceRepresentation();
        }

        if (resourceUri.equals("/grove/lcd")) {
            if (mLcdResourceA != null) {
                msg("Found another LCD resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mLcdResourceA = ocResource;

            create_list_lcd();

            getLcdResourceRepresentation();
        }

        if (resourceUri.equals("/grove/buzzer")) {
            if (mBuzzerResourceA != null) {
                msg("Found another buzzer resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mBuzzerResourceA = ocResource;

            create_list_buzzer();
        }

        if (resourceUri.equals("/grove/button")) {
            if (mButtonResourceA != null) {
                msg("Found another button resource (Arduino), ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mButtonResourceA = ocResource;

            create_list_button();

            // Call a local method which will internally invoke "get" API on the SensorResource
            getButtonResourceRepresentation();

            observeFoundButtonResource();
        }
    }

    /**
     * Local method to get representation of a found sensor resource
     */
    private void getSensorResourceRepresentation() {
        msg("Getting Sensor Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            sleep(1);
            mSensorResourceA.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"get\" API");
        }
    }

    private void getLedResourceRepresentation() {
        msg("Getting LED Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            sleep(1);
            mLedResourceA.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"get\" API");
        }
    }

    private void getLcdResourceRepresentation() {
        msg("Getting LCD Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            sleep(1);
            mLcdResourceA.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"get\" API");
        }
    }


    private void getButtonResourceRepresentation() {
        msg("Getting Button Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            sleep(1);
            mButtonResourceA.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"get\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "get" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onGetCompleted(List<OcHeaderOption> list,
                                            OcRepresentation ocRepresentation) {
        msg("GET request was successful");
        msg("Resource URI: " + ocRepresentation.getUri());

        if (ocRepresentation.getUri().equals("/grove/sensor")) {
            update_list_sensor(ocRepresentation);
        } else if(ocRepresentation.getUri().equals("/grove/led")) {
            update_list_led(ocRepresentation);
        } else if(ocRepresentation.getUri().equals("/grove/lcd")) {
            update_list_lcd(ocRepresentation);
        } else if(ocRepresentation.getUri().equals("/grove/button")) {
            update_list_button(ocRepresentation);
        }

        printLine();
    }

    /**
     * An event handler to be executed whenever a "get" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onGetFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to get representation of a found sensor resource");
    }

    /**
     * Local method to put a different state for this light resource
     */
    private void putLedRepresentation() {
        msg("Putting LED representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.ledGetOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to set OcRepresentation from LED");
        }

        Map<String, String> queryParams = new HashMap<>();

        try {
            sleep(1);
            // Invoke resource's "put" API with a new representation, query parameters and
            // OcResource.OnPutListener event listener implementation
            mLedResourceA.put(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"put\" API");
        }
    }

    private void putLcdRepresentation() {
        msg("Putting LCD representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.lcdGetOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to set OcRepresentation from LCD");
        }

        Map<String, String> queryParams = new HashMap<>();

        try {
            sleep(1);
            // Invoke resource's "put" API with a new representation, query parameters and
            // OcResource.OnPutListener event listener implementation
            mLcdResourceA.put(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"put\" API");
        }
    }

    private void putBuzzerRepresentation() {
        msg("Putting buzzer representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.buzzerGetOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to set OcRepresentation from buzzer");
        }

        Map<String, String> queryParams = new HashMap<>();

        try {
            sleep(1);
            // Invoke resource's "put" API with a new representation, query parameters and
            // OcResource.OnPutListener event listener implementation
            mBuzzerResourceA.put(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"put\" API");
        }
    }
    /**
     * An event handler to be executed whenever a "put" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onPutCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        msg("PUT request was successful");

        update_list_led(ocRepresentation);
        sendLedDoneMessage();

        update_list_lcd(ocRepresentation);

        sendBuzzerDoneMessage();

        printLine();

        //Call a local method which will internally invoke post API on the foundLightResource
        //postSensorRepresentation();
    }

    /**
     * An event handler to be executed whenever a "put" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onPutFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to \"put\" a new representation");
    }

    /**
     * Local method to post a different state for this light resource
     */
    private void postLightRepresentation() {
        //set new values
        //mDemo.setState(false);
        //mDemo.setPower(105);

        msg("Posting light representation...");
        OcRepresentation representation = null;
        try {
            representation = mDemo.sensorGetOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Failed to get OcRepresentation from a light");
        }

        Map<String, String> queryParams = new HashMap<>();
        try {
            sleep(1);
            // Invoke resource's "post" API with a new representation, query parameters and
            // OcResource.OnPostListener event listener implementation
            mSensorResourceA.post(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"post\" API");
        }
    }

    /**
     * An event handler to be executed whenever a "post" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     */
    @Override
    public synchronized void onPostCompleted(List<OcHeaderOption> list,
                                             OcRepresentation ocRepresentation) {
        msg("POST request was successful");
        try {
            if (ocRepresentation.hasAttribute(OcResource.CREATED_URI_KEY)) {
                msg("\tUri of the created resource: " +
                        ocRepresentation.getValue(OcResource.CREATED_URI_KEY));
            } else {
                mDemo.sensorSetOcRepresentation(ocRepresentation);
                //msg(mDemo.toString());
            }
        } catch (OcException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * An event handler to be executed whenever a "post" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onPostFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Failed to \"post\" a new representation");
    }

    /**
     * Local method to start observing this light resource
     */
    private void observeFoundButtonResource() {
        try {
            sleep(1);
            // Invoke resource's "observe" API with a observe type, query parameters and
            // OcResource.OnObserveListener event listener implementation
            mButtonResourceA.observe(ObserveType.OBSERVE, new HashMap<String, String>(), this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            msg("Error occurred while invoking \"observe\" API");
        }
    }


    /**
     * An event handler to be executed whenever a "post" request completes successfully
     *
     * @param list             list of the header options
     * @param ocRepresentation representation of a resource
     * @param sequenceNumber   sequence number
     */
    @Override
    public synchronized void onObserveCompleted(List<OcHeaderOption> list,
                                                OcRepresentation ocRepresentation,
                                                int sequenceNumber) {
        if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
            msg("Observe registration action is successful:");
        } else if (OcResource.OnObserveListener.DEREGISTER == sequenceNumber) {
            msg("Observe De-registration action is successful");
        } else if (OcResource.OnObserveListener.NO_OPTION == sequenceNumber) {
            msg("Observe registration or de-registration action is failed");
        }

        msg("OBSERVE Result:");
        msg("\tSequenceNumber:" + sequenceNumber);
        update_list_button(ocRepresentation);
    }

    /**
     * An event handler to be executed whenever a "observe" request fails
     *
     * @param throwable exception
     */
    @Override
    public synchronized void onObserveFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            msg("Error code: " + errCode);
        }
        msg("Observation of the found light resource has failed");
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
        mSensorResourceA = null;
        mLedResourceA = null;
        mLcdResourceA = null;
        mBuzzerResourceA = null;
        mButtonResourceA = null;
        mFoundResources.clear();
        mDemo = new DemoResource();
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
                if (id < found_devices) {
                    if (id == mDemo.getLedIndex()) {
                        LedControl led_dialog = new LedControl(MainActivity.this, mDemo.getLed());
                        led_dialog.show();
                    } else if (id == mDemo.getLcdIndex()) {
                        LcdControl lcd_dialog = new LcdControl(MainActivity.this);
                        lcd_dialog.show();
                    } else if (id == mDemo.getBuzzerIndex()) {
                        BuzzerControl buzzer_dialog = new BuzzerControl(MainActivity.this);
                        buzzer_dialog.show();
                    }
                } else {
                    msg("Out of range");
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mLedControlReceiver,
                new IntentFilter("led_adjust"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mLcdControlReceiver,
                new IntentFilter("lcd_string"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mBuzzerControlReceiver,
                new IntentFilter("buzzer_tone"));

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

    private BroadcastReceiver mLedControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int progress = intent.getIntExtra("progress", 0);
            msg("LED status: " + String.valueOf(progress));
            try {
                OcRepresentation rep = mDemo.ledGetOcRepresentation();
                rep.setValue(mDemo.LED_STATUS_KEY, progress);
                mDemo.ledSetOcRepresentation(rep);
                putLedRepresentation();
            } catch (OcException e) {
                Log.e(TAG, e.toString());
                msg("Failed to put LED status");
            }
        }
    };

    private void sendLedDoneMessage() {
        Intent intent = new Intent("led_put_done");
        // You can also include some extra data.
        intent.putExtra("put_done", true);
        msg("Send LED done message");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private BroadcastReceiver mLcdControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String str = intent.getStringExtra("string");
            msg("LCD string: " + str);
            try {
                OcRepresentation rep = mDemo.lcdGetOcRepresentation();
                rep.setValue(mDemo.LCD_KEY, str);
                mDemo.lcdSetOcRepresentation(rep);
                putLcdRepresentation();
            } catch (OcException e) {
                Log.e(TAG, e.toString());
                msg("Failed to put LCD status");
            }
        }
    };

    private BroadcastReceiver mBuzzerControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int tone = intent.getIntExtra("tone", 0);
            msg("Buzzer tone: " + String.valueOf(tone));
            try {
                OcRepresentation rep = mDemo.buzzerGetOcRepresentation();
                rep.setValue(mDemo.BUZZER_KEY, tone);
                mDemo.buzzerSetOcRepresentation(rep);
                putBuzzerRepresentation();
            } catch (OcException e) {
                Log.e(TAG, e.toString());
                msg("Failed to put Buzzer tone");
            }
        }
    };

    private void sendBuzzerDoneMessage() {
        Intent intent = new Intent("buzzer_put_done");
        // You can also include some extra data.
        intent.putExtra("put_done", true);
        msg("Send Buzzer done message");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
