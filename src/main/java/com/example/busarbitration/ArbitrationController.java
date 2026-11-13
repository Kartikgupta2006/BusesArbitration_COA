package com.example.busarbitration;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ArbitrationController {

    private final ArbitrationService arbitrationService;

    public ArbitrationController(ArbitrationService arbitrationService) {
        this.arbitrationService = arbitrationService;
    }

    @GetMapping("/status")
    public SimulatorStatus status() {
        return arbitrationService.getStatus();
    }

    @PostMapping("/request/{device}")
    public MessageResponse request(@PathVariable String device) {
        return arbitrationService.requestBus(device);
    }

    @PostMapping("/grant")
    public MessageResponse grant() {
        return arbitrationService.grantBus();
    }

    @PostMapping("/mode")
    public MessageResponse changeMode(@RequestBody ModeRequest request) {
        return arbitrationService.setMode(request.mode());
    }

    @PostMapping("/reset")
    public MessageResponse reset() {
        return arbitrationService.reset();
    }
}