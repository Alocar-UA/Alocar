package pt.ua.tqs.alocar.adapters.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ua.tqs.alocar.application.UseService;

@RestController
public class UseController {

    private final UseService useService;

    public UseController(UseService useService) {
        this.useService = useService;
    }

    /** US03: pick up a free scooter for a chosen duration, paid up front. */
    @PostMapping("/api/v1/uses")
    public ResponseEntity<UseResponse> start(@Valid @RequestBody StartUseRequest request) {
        UseResponse use = UseResponse.from(
                useService.start(request.residentId(), request.scooterId(), request.durationMinutes()));
        return ResponseEntity.created(URI.create("/api/v1/uses/" + use.id())).body(use);
    }

    @GetMapping("/api/v1/uses/{useId}")
    public UseResponse get(@PathVariable Long useId) {
        return UseResponse.from(useService.get(useId));
    }

    /** US04: return the scooter at the pick-up hub and see the fine, if any. */
    @PostMapping("/api/v1/uses/{useId}/return")
    public UseResponse returnScooter(@PathVariable Long useId, @Valid @RequestBody ReturnScooterRequest request) {
        return UseResponse.from(useService.returnScooter(useId, request.hubId()));
    }

    /** US06: the resident's current and previous uses, most recent first. */
    @GetMapping("/api/v1/residents/{residentId}/uses")
    public List<UseResponse> history(@PathVariable Long residentId) {
        return useService.history(residentId).stream().map(UseResponse::from).toList();
    }
}
