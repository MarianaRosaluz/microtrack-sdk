package org.microtrack.service;

import org.microtrack.dto.LogEvent;
import org.microtrack.dto.ResponseTrace;
import org.microtrack.gateway.CentralService;

import java.io.IOException;

public class LogService {

    private final CentralService centralService;

    public LogService() {
        this.centralService = new CentralService();
    }

    public ResponseTrace log(Manager manager, LogEvent logEvent) throws IOException, InterruptedException {
        if (!manager.isTracingEnabled()) {
            ResponseTrace responseTrace = new ResponseTrace();
            responseTrace.setMessage("Logging disabled!");
            return responseTrace;
        }

        return centralService.sendLog(logEvent);
    }

}
