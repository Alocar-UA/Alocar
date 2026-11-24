package pt.ua.tqs.alocar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.alocar.domain.Hub;
import pt.ua.tqs.alocar.domain.HubRepository;
import pt.ua.tqs.alocar.domain.ScooterRepository;
import pt.ua.tqs.alocar.domain.ScooterStatus;

@ExtendWith(MockitoExtension.class)
class HubServiceTest {

    @Mock
    private HubRepository hubRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @InjectMocks
    private HubService hubService;

    @Test
    void listHubsReturnsHubsOrderedByName() {
        List<Hub> hubs = List.of(new Hub("Hub Bloco A", "A", 8), new Hub("Hub Bloco B", "B", 6));
        when(hubRepository.findAllByOrderByNameAsc()).thenReturn(hubs);

        assertThat(hubService.listHubs()).extracting(Hub::getName).containsExactly("Hub Bloco A", "Hub Bloco B");
    }

    @Test
    void listHubsReturnsEmptyListWhenThereAreNoHubs() {
        when(hubRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        assertThat(hubService.listHubs()).isEmpty();
    }

    @Test
    void countsOnlyAvailableScooters() {
        when(scooterRepository.countByHubIdAndStatus(1L, ScooterStatus.AVAILABLE)).thenReturn(3L);

        assertThat(hubService.countAvailableScooters(1L)).isEqualTo(3);
    }

    @Test
    void listingScootersOfUnknownHubFails() {
        when(hubRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hubService.listScooters(99L)).isInstanceOf(NotFoundException.class);
    }
}
