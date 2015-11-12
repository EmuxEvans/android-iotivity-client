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
public class BuzzerResourceP implements
        OcPlatform.OnResourceFoundListener,
        OcResource.OnGetListener,
        OcResource.OnPutListener,
        OcResource.OnPostListener,
        OcResource.OnObserveListener {

    private Activity main_activity;
    private Context main_context;
    private OcPlatform.OnResourceFoundListener resource_found_listener;
    private ArrayList<String> main_list_item;
    private ArrayAdapter<String> main_list_adapter;
    private Map<OcResourceIdentifier, OcResource> mFoundResources = new HashMap<>();
    private OcResource mResource = null;
    private Thread find_thread = null;
    private boolean find_thread_running;

    private int mBuzzer;
    private int mBuzzerListIndex;
    private final static String TAG = "RaspberryPi2 Buzzer";

    private final static String resource_type = "grovepi.buzzer";
    private final static String resource_uri = "/grovepi/buzzer";
    private static final String BUZZER_KEY = "buzzer";

    public final static String buzzer_display = "(RaspberryPi2) Buzzer: ";
    public final static String msg_found = "msg_found_resource";
    public final static String msg_put_done = "msg_buzzer_put_done_p";


    public BuzzerResourceP(Activity main, Context c, ArrayList<String> list_item,
                           ArrayAdapter<String> list_adapter) {
        main_activity = main;
        main_context = c;
        resource_found_listener = this;
        find_thread_running = true;

        main_list_item = list_item;
        main_list_adapter = list_adapter;

        mBuzzer = 0;
        mBuzzerListIndex = -1;
    }

    public void setOcRepresentation(OcRepresentation rep) throws OcException {
        mBuzzer = rep.getValue(BUZZER_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(BUZZER_KEY, mBuzzer);
        return rep;
    }

    public void setBuzzerIndex(int index) { mBuzzerListIndex = index; }
    public int getBuzzerIndex() { return mBuzzerListIndex; }

    public void find_resource() {
        find_thread = new Thread(new Runnable() {
            public void run() {
                String requestUri;

                Log.e(TAG, "Finding resources of type: " + resource_type);
                requestUri = OcPlatform.WELL_KNOWN_QUERY + "?rt=" + resource_type;

                while(mResource == null && !Thread.interrupted() && find_thread_running) {
                    try {
                        OcPlatform.findResource("",
                                requestUri,
                                EnumSet.of(OcConnectivityType.CT_DEFAULT),
                                resource_found_listener
                        );
                    } catch (OcException e) {
                        Log.e(TAG, e.toString());
                        Log.e(TAG, "Failed to invoke find resource API");
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException");
                        return;
                    }
                }
            }
        });

        find_thread.start();
    }

    public void stop_find_thread() {
        if(find_thread != null) {
            find_thread_running = false;
            find_thread.interrupt();
        }
    }

    public void getResourceRepresentation() {
        Log.e(TAG, "Update buzzer UI");
        update_list();
    }

    public void putResourceRepresentation(int buzzer) {
        Log.e(TAG, "Putting buzzer representation...");
        mBuzzer = buzzer;

        OcRepresentation representation = null;
        try {
            representation = getOcRepresentation();
        } catch (OcException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Failed to set OcRepresentation from buzzer");
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
        stop_find_thread();
        mFoundResources.clear();
    }

    public void update_list() {
        main_activity.runOnUiThread(new Runnable() {
            public synchronized void run() {
                main_list_item.set(mBuzzerListIndex, buzzer_display);
                main_list_adapter.notifyDataSetChanged();
            }
        });
    }

    private void sendBroadcastMessage(String type, String key, boolean b) {
        Intent intent = new Intent(type);

        intent.putExtra(key, b);
        Log.e(TAG, "Send buzzer message: " + type + ", " + key + ", " + String.valueOf(b));
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
                Log.e(TAG, "Found another Arduino buzzer resource, ignoring");
                return;
            }

            //Assign resource reference to a global variable to keep it from being
            //destroyed by the GC when it is out of scope.
            mResource = ocResource;

            sendBroadcastMessage(msg_found, "buzzer_found_resource_p", true);
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
            Log.e(TAG, "Failed to set buzzer representation");
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

        Log.e(TAG, "Failed to get representation of a found buzzer resource");
    }

    @Override
    public synchronized void onPutCompleted(List<OcHeaderOption> list, OcRepresentation ocRepresentation) {
        Log.e(TAG, "PUT request was successful");
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
            Log.e(TAG, "RaspberryPi2 buzzer observe registration action is successful:");
        } else if (OcResource.OnObserveListener.DEREGISTER == sequenceNumber) {
            Log.e(TAG, "RaspberryPi2 buzzer observe De-registration action is successful");
        } else if (OcResource.OnObserveListener.NO_OPTION == sequenceNumber) {
            Log.e(TAG, "RaspberryPi2 buzzer observe registration or de-registration action is failed");
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
        Log.e(TAG, "Observation of the found buzzer resource has failed");
    }
}
