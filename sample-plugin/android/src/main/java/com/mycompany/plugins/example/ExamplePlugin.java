package com.mycompany.plugins.example;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.concurrent.TimeUnit;


@CapacitorPlugin(name = "ExamplePlugin")
public class ExamplePlugin extends Plugin {

    private static int counter = 1;
    public static int getCounterValue() {
        return counter;
    }
    private Constraints constraints;
    private Callback backgroundWorkCallback;
    String workId = "";
    int status = 0;

    @PluginMethod
    public void setCallback(PluginCall call) {
        backgroundWorkCallback = new Callback() {
            @Override
            public void onWorkCompleted(String workId) {
                JSObject result = new JSObject();
                result.put("workId", workId);
                call.resolve(result);
            }
        };
        call.resolve();
    }

    @Override
    public void load() {
        constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
    }


    @PluginMethod
    public void startWork(PluginCall call) {
        int interval = call.getInt("interval", 1);
        workId = "id " + counter;
        status = 99;

        PeriodicWorkRequest myRequest = new PeriodicWorkRequest.Builder(
                Example.class, interval, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
                workId, ExistingPeriodicWorkPolicy.KEEP, myRequest
        );

        // Observe work status on the main thread using runOnUiThread
        getActivity().runOnUiThread(() -> {
            WorkManager.getInstance(getContext())
                    .getWorkInfoByIdLiveData(myRequest.getId())
                    .observeForever(workInfo -> {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            if (backgroundWorkCallback != null) {
                                backgroundWorkCallback.onWorkCompleted(workId);
                                status = 1;
                            }
                        }
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                            if (backgroundWorkCallback != null) {
                                backgroundWorkCallback.onWorkCompleted("failure");
                                status = -1;
                            }
                        }
                    });
        });

        counter++;
    }

    @PluginMethod
    public void displayStatus(PluginCall call) {
        JSObject result = new JSObject();
        result.put("value", status);
        call.resolve(result);
    }

    @PluginMethod
    public void getFileCount(PluginCall call) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            int count = dbHelper.getFileCount();

            JSObject result = new JSObject();
            result.put("value", count);
            call.resolve(result);
        } catch (Exception e) {
            JSObject errorResult = new JSObject();
            errorResult.put("error", "An error occurred while getting file count.");
            call.reject("FILE_COUNT_ERROR", e, errorResult);
        }
    }

    @PluginMethod
    public void stopWork(PluginCall call) {
        status = 0;
        if (workId != ""){
            WorkManager.getInstance(getContext()).cancelUniqueWork(workId);
            call.resolve();
        }
    }

    public interface Callback {
        void onWorkCompleted(String workId);
    }
}