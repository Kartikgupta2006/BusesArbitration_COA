package com.example.busarbitration;

import java.time.LocalTime;
import java.util.List;

enum ArbitrationMode {
    FIFO,
    PRIORITY,
    ROUND_ROBIN
}

record DeviceInfo(String name, int priority) {}

record BusRequest(String device, long sequence, LocalTime requestedAt) {}

record GrantRecord(
        int cycle,
        String device,
        String mode,
        LocalTime grantedAt
) {}

record SimulatorStatus(
        String mode,
        String busOwner,
        List<BusRequest> waitingRequests,
        List<GrantRecord> history,
        List<DeviceInfo> devices,
        int totalGrants,
        int totalRequests
) {}

record MessageResponse(String message) {}

record ModeRequest(String mode) {}