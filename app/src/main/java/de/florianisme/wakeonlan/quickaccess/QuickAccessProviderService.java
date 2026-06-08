package de.florianisme.wakeonlan.quickaccess;

import android.os.Build;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.actions.ControlAction;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.reactivestreams.FlowAdapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import de.florianisme.wakeonlan.persistence.models.Device;
import de.florianisme.wakeonlan.persistence.repository.DeviceRepository;
import de.florianisme.wakeonlan.util.AppLogger;
import de.florianisme.wakeonlan.wol.WolSender;
import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;

@RequiresApi(api = Build.VERSION_CODES.R)
public class QuickAccessProviderService extends ControlsProviderService {

    private static final String TAG = "QuickAccessProviderService";

    private final Map<String, ReplayProcessor<Control>> processorMap = new HashMap<>();

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {
        AppLogger.i(TAG, "createPublisherForAllAvailable");
        try {
            return FlowAdapters.toFlowPublisher(Flowable.fromIterable(StatelessControlService.createStatelessControls(this)));
        } catch (Exception e) {
            AppLogger.e(TAG, "Error in createPublisherForAllAvailable", e);
            return FlowAdapters.toFlowPublisher(Flowable.empty());
        }
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        AppLogger.i(TAG, "createPublisherFor, ids: " + controlIds);
        ReplayProcessor<Control> processor = ReplayProcessor.create();
        controlIds.forEach(id -> processorMap.put(id, processor));

        try {
            StatefulControlService.createAndUpdateStatefulControls(controlIds, processor, this);
        } catch (Exception e) {
            AppLogger.e(TAG, "Error in createPublisherFor", e);
            processor.onError(e);
        }

        return FlowAdapters.toFlowPublisher(processor);
    }

    @Override
    public void onDestroy() {
        AppLogger.i(TAG, "onDestroy");
        try {
            StatefulControlService.stopAllStatusTesters();
        } catch (Exception e) {
            AppLogger.e(TAG, "Error stopping status testers in onDestroy", e);
        }
        super.onDestroy();
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        AppLogger.i(TAG, "performControlAction, controlId: " + controlId);
        ReplayProcessor<Control> processor = processorMap.get(controlId);
        if (processor == null) {
            consumer.accept(ControlAction.RESPONSE_FAIL);
            return;
        }

        try {
            consumer.accept(ControlAction.RESPONSE_OK);

            DeviceRepository deviceRepository = DeviceRepository.getInstance(this);
            Device device = deviceRepository.getById(Integer.parseInt(controlId));

            if (device != null) {
                WolSender.sendWolPacket(device);
                StatefulControlService.createAndUpdateStatefulControl(controlId, processor, this);
            }
        } catch (Exception e) {
            AppLogger.e(TAG, "Error in performControlAction", e);
            consumer.accept(ControlAction.RESPONSE_FAIL);
        }
    }
}
