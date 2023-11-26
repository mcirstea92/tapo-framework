package ro.builditsmart.rest.tapo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.builditsmart.models.tapo.method.requests.LoginRequest;
import ro.builditsmart.models.tapo.method.responses.LoginResponse;
import ro.builditsmart.rest.tapo.services.SessionManagerService;
import ro.builditsmart.rest.tapo.services.TapoClientService;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final TapoClientService clientService;

    private final SessionManagerService sessionManagerService;

    public LoginController(TapoClientService clientService, SessionManagerService sessionManagerService) {
        this.clientService = clientService;
        this.sessionManagerService = sessionManagerService;
    }

    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body) {
        var client = clientService.getClient(body.getProtocol());
        var deviceKey = client.loginByIpAsync(body.getIp(), body.getUsername(), body.getPassword()).join();
        var loginResponse = new LoginResponse(deviceKey);
        sessionManagerService.addSessionInfo(deviceKey.getSessionCookie(), deviceKey, loginResponse);
        return ResponseEntity.ok(loginResponse);
    }

}
