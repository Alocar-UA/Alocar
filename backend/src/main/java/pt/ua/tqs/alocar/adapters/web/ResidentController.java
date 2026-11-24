package pt.ua.tqs.alocar.adapters.web;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ua.tqs.alocar.application.ResidentService;

@RestController
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    /** US01: create an account through the residence's join link; it joins that subfleet (BR2). */
    @PostMapping("/api/v1/residents")
    public ResponseEntity<ResidentResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        ResidentResponse resident = ResidentResponse.from(
                residentService.signUp(request.joinToken(), request.name(), request.email()));
        return ResponseEntity.created(URI.create("/api/v1/residents/" + resident.id())).body(resident);
    }
}
