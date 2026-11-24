package pt.ua.tqs.alocar.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.tqs.alocar.domain.Hub;
import pt.ua.tqs.alocar.domain.HubRepository;
import pt.ua.tqs.alocar.domain.Scooter;
import pt.ua.tqs.alocar.domain.ScooterRepository;
import pt.ua.tqs.alocar.domain.ScooterStatus;

@Service
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;
    private final ScooterRepository scooterRepository;

    public HubService(HubRepository hubRepository, ScooterRepository scooterRepository) {
        this.hubRepository = hubRepository;
        this.scooterRepository = scooterRepository;
    }

    public List<Hub> listHubs() {
        return hubRepository.findAllByOrderByNameAsc();
    }

    /** US02: how many scooters are free at the hub right now. */
    public long countAvailableScooters(Long hubId) {
        return scooterRepository.countByHubIdAndStatus(hubId, ScooterStatus.AVAILABLE);
    }

    public List<Scooter> listScooters(Long hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new NotFoundException("Hub " + hubId + " not found");
        }
        return scooterRepository.findByHubIdOrderByCodeAsc(hubId);
    }
}
