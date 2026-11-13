package com.example.busarbitration;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class ArbitrationService {

    private final List<DeviceInfo> devices = List.of(
            new DeviceInfo("CPU", 3),
            new DeviceInfo("DMA", 2),
            new DeviceInfo("Disk", 1),
            new DeviceInfo("Network", 1)
    );

    private final List<BusRequest> waiting = new ArrayList<>();
    private final List<GrantRecord> history = new ArrayList<>();

    private ArbitrationMode mode = ArbitrationMode.FIFO;
    private String busOwner = "Free";
    private long sequence = 0;
    private int roundRobinIndex = -1;

    public synchronized MessageResponse requestBus(String deviceName) {
        DeviceInfo device = findDevice(deviceName);

        if (device == null) {
            throw new IllegalArgumentException("Unknown device: " + deviceName);
        }

        boolean alreadyWaiting = waiting.stream()
                .anyMatch(request -> request.device().equalsIgnoreCase(deviceName));

        if (alreadyWaiting) {
            return new MessageResponse(deviceName + " is already waiting.");
        }

        waiting.add(new BusRequest(
                device.name(),
                ++sequence,
                LocalTime.now().withNano(0)
        ));

        return new MessageResponse(device.name() + " request added to the queue.");
    }

    public synchronized MessageResponse grantBus() {
        if (waiting.isEmpty()) {
            busOwner = "Free";
            return new MessageResponse("No requests are waiting.");
        }

        BusRequest selected = switch (mode) {
            case FIFO -> selectFifo();
            case PRIORITY -> selectPriority();
            case ROUND_ROBIN -> selectRoundRobin();
        };

        waiting.remove(selected);
        busOwner = selected.device();

        history.add(new GrantRecord(
                history.size() + 1,
                selected.device(),
                mode.name(),
                LocalTime.now().withNano(0)
        ));

        return new MessageResponse(
                selected.device() + " was granted the bus using " + mode + "."
        );
    }

    public synchronized SimulatorStatus getStatus() {
        return new SimulatorStatus(
                mode.name(),
                busOwner,
                List.copyOf(waiting),
                List.copyOf(history),
                devices,
                history.size(),
                (int) sequence
        );
    }

    public synchronized MessageResponse setMode(String modeName) {
        try {
            mode = ArbitrationMode.valueOf(modeName.toUpperCase(Locale.ROOT));
            return new MessageResponse("Arbitration mode changed to " + mode + ".");
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Mode must be FIFO, PRIORITY, or ROUND_ROBIN."
            );
        }
    }

    public synchronized MessageResponse reset() {
        waiting.clear();
        history.clear();
        busOwner = "Free";
        sequence = 0;
        roundRobinIndex = -1;
        return new MessageResponse("Simulator reset.");
    }

    private BusRequest selectFifo() {
        return waiting.stream()
                .min(Comparator.comparingLong(BusRequest::sequence))
                .orElseThrow();
    }

    private BusRequest selectPriority() {
        return waiting.stream()
                .max(Comparator
                        .comparingInt((BusRequest request) ->
                                findDevice(request.device()).priority())
                        .thenComparingLong(request -> -request.sequence()))
                .orElseThrow();
    }

    private BusRequest selectRoundRobin() {
        for (int step = 1; step <= devices.size(); step++) {
            int index = (roundRobinIndex + step) % devices.size();
            String nextDevice = devices.get(index).name();

            Optional<BusRequest> request = waiting.stream()
                    .filter(item -> item.device().equals(nextDevice))
                    .findFirst();

            if (request.isPresent()) {
                roundRobinIndex = index;
                return request.get();
            }
        }

        return selectFifo();
    }

    private DeviceInfo findDevice(String name) {
        return devices.stream()
                .filter(device -> device.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}