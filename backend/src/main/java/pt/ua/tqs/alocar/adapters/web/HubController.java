package pt.ua.tqs.alocar.adapters.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ua.tqs.alocar.application.HubService;

@RestController
@RequestMapping("/api/v1/hubs")
public class HubController {

    private final HubService hubService;

    public HubController(HubService hubService) {
        this.hubService = hubService;
    }

    @GetMapping
    public List<HubResponse> listHubs() {
        return hubService.listHubs().stream()
                .map(hub -> HubResponse.from(hub, hubService.countAvailableScooters(hub.getId())))
                .toList();
    }

    @GetMapping("/{hubId}/scooters")
    public List<ScooterResponse> listScooters(@PathVariable Long hubId) {
        return hubService.listScooters(hubId).stream().map(ScooterResponse::from).toList();
    }
}
