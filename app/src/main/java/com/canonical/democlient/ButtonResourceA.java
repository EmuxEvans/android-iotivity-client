package com.canonical.democlient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.iotivity.base.ErrorCode;
import org.iotivity.base.ObserveType;
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
public class ButtonResourceA implements
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
    private int mButton;
    private int mTouch;
    private int mButtonListIndex;
    private int mTouchListIndex;
    private final static String TAG = "Arduino Button Class";

    private final static String resource_type = "grove.button";
    private final static String resource_uri = "/grove/button";
    private static final String BUTTON_KEY = "button";
    private static final String BUTTON_TOUCH_KEY = "touch";

    public final static String button_display = "(Arduino) Button: ";
    public final static String button_touch_display = "(Arduino) Touch: ";
    public final static String msg_found = "msg_found_a";

    public ButtonResourceA(Activity main, Context c, ArrayList<String> list_item,
                           ArrayAdapter<String> list_adapter) {
        main_activity = main;
        main_context = c;
        main_list_item = list_item;
        main_list_adapter = list_adapter;

        mButton = 0;
        mTouch = 0;
        mButtonListIndex = -1;
        mTouchListIndex = -1;
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mButton = rep.getValue(BUTTON_KEY);
        mTouch = rep.getValue(BUTTON_TOUCH_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(BUTTON_KEY, mButton);
        rep.setValue(BUTTON_TOUCH_KEY, mTouch);
        return rep;
    }

    public int getButton() { return mButton; }
    public int getTouch() { return mTouch; };

    public void setButtonIndex(int index) { mButtonListIndex = index; }
    public void setTouchIndex(int index) { mTouchListIndex = index; }
    public int getButtonIndex() { return mButtonListIndex; }
    public int getTouchIndex() { return mTouchListIndex; }

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
        Log.e(TAG, "Getting button Representation...");

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

    public void observeFoundResource() {
        try {
            // Invoke resource's "observe" API with a observe type, query parameters and
            // OcResource.OnObserveListener event listener implementation
            mResource.observe(ObserveType.OBSERVE, new HashMap<String, String>(), this);
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Error occurred while invoking \"observe\" API");
        }
    }

    public void reset() {
        mResource = null;
    }

    private void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mButtonListIndex, button_display + String.valueOf(mButton));
                main_list_item.set(mTouchListIndex, button_touch_display + String.valueOf(mTouch));
                main_list_adapter.notifyDataSetChanged();

                Log.e(TAG, "Button:");
                Log.e(TAG, String.valueOf(mButton));
                Log.e(TAG, String.valueOf(mTouch));
            }
        });
    }

    private void sendBroadcastMessage(String type, String key, boolean b) {
        Intent intent = new Intent(type);

        intent.putExtra(key, b);
        Log.e(TAG, "Send button message: " + type + ", " + key + ", " + String.valueOf(b));
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
                Log.e(TAG, "Found another Arduino button resource, ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mResource = ocResource;

            sendBroadcastMessage(msg_found, "button_found_resource", true);
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
            Log.e(TAG, "Failed to set button representation");
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

        Log.e(TAG, "Failed to get representation of a found button resource");
    }

    @Override
    public synchronized void onPutCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        Log.e(TAG, "PUT request was successful");
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
            Log.e(TAG, "Button observe registration action is successful:");
        } else if (OcResource.OnObserveListener.DEREGISTER == sequenceNumber) {
            Log.e(TAG, "Button Observe De-registration action is successful");
        } else if (OcResource.OnObserveListener.NO_OPTION == sequenceNumber) {
            Log.e(TAG, "Button Observe registration or de-registration action is failed");
        }

        Log.e(TAG, "OBSERVE Result:");
        Log.e(TAG, "\tSequenceNumber:" + sequenceNumber);

        try {
            setOcRepresentation(ocRepresentation);
            update_list();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Failed to set button representation");
        }
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
        Log.e(TAG, "Observation of the found button resource has failed");
    }
}
