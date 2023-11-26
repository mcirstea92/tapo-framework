package ro.builditsmart.rest.tapo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.builditsmart.rest.tapo.services.SessionManagerService;

@CrossOrigin(origins = "http://localhost:8081")
@RestController("device-controller")
@RequestMapping("/device")
@Slf4j
public class TapoDeviceController {

    private final SessionManagerService sessionManagerService;

    public TapoDeviceController(SessionManagerService sessionManagerService) {
        this.sessionManagerService = sessionManagerService;
    }

    @DeleteMapping("invalidate")
    public ResponseEntity<HttpStatus> invalidateSession(@RequestHeader(HttpHeaders.COOKIE) String cookie) {
        if (sessionManagerService.invalidateSession(cookie)) {
            return ResponseEntity.noContent().build();
        }
        log.error("Could not find a valid session with id: {}", cookie);
        return ResponseEntity.internalServerError().build();
    }
}
