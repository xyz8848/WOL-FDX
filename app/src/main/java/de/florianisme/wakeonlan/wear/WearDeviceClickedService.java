package de.florianisme.wakeonlan.wear;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.persistence.repository.DeviceRepository;
import de.florianisme.wakeonlan.wol.WolSender;

public class WearDeviceClickedService extends WearableListenerService {

    private static final String TAG = "WearDeviceClickedService";
    private static final String DEVICE_CLICKED_PATH = "/device_clicked";

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        try {
            if (messageEvent.getPath().equals(DEVICE_CLICKED_PATH)) {
                int deviceId = messageEvent.getData()[0];

                DeviceRepository deviceRepository = DeviceRepository.getInstance(this);
                Device device = deviceRepository.getById(deviceId);

                if (device != null) {
                    WolSender.sendWolPacket(device);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to handle Wear message", e);
        }
    }
}
