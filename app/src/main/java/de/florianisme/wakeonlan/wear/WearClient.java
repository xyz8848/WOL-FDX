package de.florianisme.wakeonlan.wear;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import de.florianisme.wakeonlan.models.DeviceDto;
import de.florianisme.wakeonlan.persistence.models.Device;

public class WearClient {

    private static final String TAG = "WearClient";
    private static final String DEVICE_LIST_PATH = "/device_list";
    private DataClient dataClient;
    private boolean isAvailable = false;

    public WearClient(Context context) {
        if (GooglePlayServicesHelper.isGooglePlayServicesAvailable(context)) {
            try {
                dataClient = Wearable.getDataClient(context);
                isAvailable = true;
            } catch (Exception e) {
                Log.w(TAG, "Failed to initialize Wear DataClient", e);
            }
        } else {
            Log.w(TAG, "Google Play Services not available, Wear features disabled");
        }
    }

    public void onDeviceListUpdated(List<Device> deviceList) {
        if (!isAvailable) {
            return;
        }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DEVICE_LIST_PATH);
        putDataMapRequest.getDataMap().putByteArray("devices", buildDevicesListByteArray(deviceList));
        PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();

        try {
            dataClient.putDataItem(putDataReq);
        } catch (Exception e) {
            Log.w(TAG, "Failed to sync device list to Wear", e);
        }
    }

    private byte[] buildDevicesListByteArray(List<Device> devices) {
        try {
            List<DeviceDto> deviceDtos = devices.stream()
                    .map(device -> new DeviceDto(device.id, device.name))
                    .collect(Collectors.toList());
            return new Gson().toJson(deviceDtos).getBytes(StandardCharsets.UTF_8);
        } catch (JsonParseException e) {
            Log.e(getClass().getSimpleName(), "Could not transform list of devices to byte array", e);
            return new byte[0];
        }
    }
}
