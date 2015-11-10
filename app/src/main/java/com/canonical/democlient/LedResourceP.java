package com.canonical.democlient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.iotivity.base.ErrorCode;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcHeaderOption;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResourceIdentifier;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gerald on 2015/11/10.
 */
public class LedResourceP implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener {

    private Activity main_activity;
    private Context main_context;
    private ArrayList<String> main_list_item;
    private ArrayAdapter<String> main_list_adapter;
    private Map<OcResourceIdentifier, OcResource> mFoundResources = new HashMap<>();
    private OcResource mResource = null;

    private int mLedRed;
    private int mLedGreen;
    private int mLedBlue;
    private int mLedRedListIndex;
    private int mLedGreenListIndex;
    private int mLedBlueListIndex;
    private final static String TAG = "RaspberryPi2 LED";

    private final static String resource_type = "grovepi.led";
    private final static String resource_uri = "/grovepi/led";
    private final static String LED_RED_KEY = "red";
    private final static String LED_GREEN_KEY = "green";
    private final static String LED_BLUE_KEY = "blue";

    public final static String led_red_display = "(RaspberryPi2) LED red: ";
    public final static String led_green_display = "(RaspberryPi2) LED green: ";
    public final static String led_blue_display = "(RaspberryPi2) LED blue: ";
    public final static String msg_found = "msg_found_resource";
    public final static String msg_put_done = "msg_led_put_done_p";


    public LedResourceP(Activity main, Context c, ArrayList<String> list_item,
                        ArrayAdapter<String> list_adapter) {
        main_activity = main;
        main_context = c;
        main_list_item = list_item;
        main_list_adapter = list_adapter;

        mLedRed = 0;
        mLedGreen = 0;
        mLedBlue = 0;
        mLedRedListIndex = -1;
        mLedGreenListIndex = -1;
        mLedBlueListIndex = -1;
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mLedRed = rep.getValue(LED_RED_KEY);
        mLedGreen = rep.getValue(LED_GREEN_KEY);
        mLedBlue = rep.getValue(LED_BLUE_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(LED_RED_KEY, mLedRed);
        rep.setValue(LED_GREEN_KEY, mLedGreen);
        rep.setValue(LED_BLUE_KEY, mLedBlue);
        return rep;
    }

    public void setLedRedIndex(int index) { mLedRedListIndex = index; }
    public void setLedGreenIndex(int index) { mLedGreenListIndex = index; }
    public void setLedBlueIndex(int index) { mLedBlueListIndex = index; }

    public int getLedRedIndex() { return mLedRedListIndex; }
    public int getLedGreenIndex() { return mLedGreenListIndex; }
    public int getLedBlueIndex() { return mLedBlueListIndex; }

    public void find_resource() {
        String requestUri;

        Log.e(TAG, "Finding resources of type: " + resource_type);
        requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resource_type;

        try {
            OcPlatform.findResource("",
                    requestUri,
                    EnumSet.of(OcConnectivityType.CT_DEFAULT),
                    this
            );
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Failed to invoke find resource API");
        }
    }

    public void getResourceRepresentation() {
        Log.e(TAG, "Getting LED Representation...");

        Map<String, String> queryParams = new HashMap<>();
        try {
            // Invoke resource's "get" API with a OcResource.OnGetListener event
            // listener implementation
            mResource.get(queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Error occurred while invoking \"get\" API");
        }
    }

    public void putResourceRepresentation(int rgb, int status) {
        Log.e(TAG, "Putting LED representation...");
        switch(rgb) {
            case 0:
                mLedRed = status;
                break;
            case 1:
                mLedGreen = status;
                break;
            case 2:
                mLedBlue = status;
                break;
        }

        OcRepresentation representation = null;
        try {
            representation = getOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Failed to set OcRepresentation from LED");
        }

        Map<String, String> queryParams = new HashMap<>();

        try {
            // Invoke resource's "put" API with a new representation, query parameters and
            // OcResource.OnPutListener event listener implementation
            mResource.put(representation, queryParams, this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Error occurred while invoking \"put\" API");
        }
    }

    public void reset() {
        mResource = null;
    }

    private void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mLedRedListIndex, led_red_display + String.valueOf(mLedRed));
                main_list_item.set(mLedGreenListIndex, led_green_display + String.valueOf(mLedGreen));
                main_list_item.set(mLedBlueListIndex, led_blue_display + String.valueOf(mLedBlue));
                main_list_adapter.notifyDataSetChanged();

                Log.e(TAG, "Raspberry Pi 2 LED status:");
                Log.e(TAG, String.valueOf(mLedRed));
                Log.e(TAG, String.valueOf(mLedGreen));
                Log.e(TAG, String.valueOf(mLedBlue));
            }
        });
    }

    private void sendBroadcastMessage(String type, String key, boolean b) {
        Intent intent = new Intent(type);

        intent.putExtra(key, b);
        Log.e(TAG, "Send LED message: " + type + ", " + key + ", " + String.valueOf(b));
        LocalBroadcastManager.getInstance(main_context).sendBroadcast(intent);
    }

    @Override
    public synchronized void onResourceFound(OcResource ocResource) {
        if (null == ocResource) {
            Log.e(TAG, "Found resource is invalid");
            return;
        }

        if (mFoundResources.containsKey(ocResource.getUniqueIdentifier())) {
            Log.e(TAG, "Found a previously seen resource again!");
        } else {
            Log.e(TAG, "Found resource for the first time on server with ID: " + ocResource.getServerId());
            mFoundResources.put(ocResource.getUniqueIdentifier(), ocResource);
        }

        // Get the resource URI
        String resourceUri = ocResource.getUri();
        // Get the resource host address
        String hostAddress = ocResource.getHost();
        Log.e(TAG, "\tURI of the resource: " + resourceUri);
        Log.e(TAG, "\tHost address of the resource: " + hostAddress);
        // Get the resource types
        Log.e(TAG, "\tList of resource types: ");
        for (String resourceType : ocResource.getResourceTypes()) {
            Log.e(TAG, "\t\t" + resourceType);
        }
        Log.e(TAG, "\tList of resource interfaces:");
        for (String resourceInterface : ocResource.getResourceInterfaces()) {
            Log.e(TAG, "\t\t" + resourceInterface);
        }
        Log.e(TAG, "\tList of resource connectivity types:");
        for (OcConnectivityType connectivityType : ocResource.getConnectivityTypeSet()) {
            Log.e(TAG, "\t\t" + connectivityType);
        }

        if (resourceUri.equals(resource_uri)) {
            if (mResource != null) {
                Log.e(TAG, "Found another Raspberry Pi 2 LED resource, ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mResource = ocResource;

            sendBroadcastMessage(msg_found, "led_found_resource_p", true);
        }
    }

    @Override
    public synchronized void onGetCompleted(List<OcHeaderOption> list,
                                            OcRepresentation ocRepresentation) {
        Log.e(TAG, "GET request was successful");
        Log.e(TAG, "Resource URI: " + ocRepresentation.getUri());

        try {
            setOcRepresentation(ocRepresentation);
            update_list();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Failed to set LED representation");
        }
    }

    @Override
    public synchronized void onGetFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            Log.e(TAG, "Error code: " + errCode);
        }

        Log.e(TAG, "Failed to get representation of a found LED resource");
    }

    @Override
    public synchronized void onPutCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        Log.e(TAG, "PUT request was successful");
        update_list();
        sendBroadcastMessage(msg_put_done, "put_done", true);
    }

    @Override
    public synchronized void onPutFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            Log.e(TAG, "Error code: " + errCode);
        }
        Log.e(TAG, "Failed to \"put\" a new representation");
    }

    @Override
    public synchronized void onPostCompleted(List<OcHeaderOption> list,
                                             OcRepresentation ocRepresentation) {
        Log.e(TAG, "POST request was successful");
        try {
            if (ocRepresentation.hasAttribute(OcResource.CREATED_URI_KEY)) {
                Log.e(TAG, "\tUri of the created resource: " +
                        ocRepresentation.getValue(OcResource.CREATED_URI_KEY));
            } else {
                setOcRepresentation(ocRepresentation);
            }
        } catch (OcException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public synchronized void onPostFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            Log.e(TAG, "Error code: " + errCode);
        }
        Log.e(TAG, "Failed to \"post\" a new representation");
    }

    @Override
    public synchronized void onObserveCompleted(List<OcHeaderOption> list,
                                                OcRepresentation ocRepresentation,
                                                int sequenceNumber) {
        if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
            Log.e(TAG, "RaspberryPi2 LED observe registration action is successful:");
        } else if (OcResource.OnObserveListener.DEREGISTER == sequenceNumber) {
            Log.e(TAG, "RaspberryPi2 LED observe De-registration action is successful");
        } else if (OcResource.OnObserveListener.NO_OPTION == sequenceNumber) {
            Log.e(TAG, "RaspberryPi2 LED observe registration or de-registration action is failed");
        }

        Log.e(TAG, "OBSERVE Result:");
        Log.e(TAG, "\tSequenceNumber:" + sequenceNumber);
        //update_list();
    }

    @Override
    public synchronized void onObserveFailed(Throwable throwable) {
        if (throwable instanceof OcException) {
            OcException ocEx = (OcException) throwable;
            Log.e(TAG, ocEx.toString());
            ErrorCode errCode = ocEx.getErrorCode();
            //do something based on errorCode
            Log.e(TAG, "Error code: " + errCode);
        }
        Log.e(TAG, "Observation of the found LED resource has failed");
    }
}
