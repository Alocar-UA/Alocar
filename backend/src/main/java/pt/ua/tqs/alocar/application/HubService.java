package pt.ua.tqs.alocar.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.tqs.alocar.domain.Hub;
import pt.ua.tqs.alocar.domain.HubRepository;

@Service
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;

    public HubService(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    public List<Hub> listHubs() {
        return hubRepository.findAllByOrderByNameAsc();
    }
}
