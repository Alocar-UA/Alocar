package pt.ua.tqs.alocar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.tqs.alocar.domain.Resident;
import pt.ua.tqs.alocar.domain.ResidentRepository;
import pt.ua.tqs.alocar.domain.Subfleet;
import pt.ua.tqs.alocar.domain.SubfleetRepository;

/** UC1 / US01: create an account through the residence's join link. */
@ExtendWith(MockitoExtension.class)
class ResidentServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private SubfleetRepository subfleetRepository;

    @InjectMocks
    private ResidentService service;

    private Subfleet riaNova;

    @BeforeEach
    void setUp() {
        riaNova = new Subfleet("Residência Ria Nova");
        lenient().when(subfleetRepository.findByJoinToken(riaNova.getJoinToken())).thenReturn(Optional.of(riaNova));
        lenient().when(residentRepository.save(any(Resident.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void residentWhoSignsUpThroughTheLinkBelongsToItsSubfleet() {
        Resident marta = service.signUp(riaNova.getJoinToken(), "Marta Lopes", "marta@exemplo.pt");

        assertThat(marta.getSubfleet()).isSameAs(riaNova);
        assertThat(marta.getName()).isEqualTo("Marta Lopes");
        verify(residentRepository).save(marta);
    }

    @Test
    void unknownJoinLinkIsRefused() {
        when(subfleetRepository.findByJoinToken("link-que-nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.signUp("link-que-nao-existe", "Marta Lopes", "marta@exemplo.pt"))
                .isInstanceOf(NotFoundException.class);
        verify(residentRepository, never()).save(any());
    }

    @Test
    void disabledJoinLinkIsRefused() {
        riaNova.disableJoinLink();

        assertThatThrownBy(() -> service.signUp(riaNova.getJoinToken(), "Marta Lopes", "marta@exemplo.pt"))
                .isInstanceOf(JoinLinkDisabledException.class);
        verify(residentRepository, never()).save(any());
    }

    /** BR2: one account, one subfleet; the email is compared in lower case. */
    @Test
    void br2EmailAlreadyRegisteredIsRefused() {
        when(residentRepository.existsByEmail("marta@exemplo.pt")).thenReturn(true);

        assertThatThrownBy(() -> service.signUp(riaNova.getJoinToken(), "Marta Lopes", " MARTA@exemplo.pt"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("rule")
                .isEqualTo("BR2");
        verify(residentRepository, never()).save(any());
    }
}
