package pt.ua.tqs.alocar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ua.tqs.alocar.application.port.PaymentGateway;
import pt.ua.tqs.alocar.domain.Hub;
import pt.ua.tqs.alocar.domain.Payment;
import pt.ua.tqs.alocar.domain.PaymentKind;
import pt.ua.tqs.alocar.domain.PaymentRepository;
import pt.ua.tqs.alocar.domain.PaymentStatus;
import pt.ua.tqs.alocar.domain.Resident;
import pt.ua.tqs.alocar.domain.ResidentRepository;
import pt.ua.tqs.alocar.domain.Scooter;
import pt.ua.tqs.alocar.domain.ScooterRepository;
import pt.ua.tqs.alocar.domain.ScooterStatus;
import pt.ua.tqs.alocar.domain.Subfleet;
import pt.ua.tqs.alocar.domain.Tariff;
import pt.ua.tqs.alocar.domain.Use;
import pt.ua.tqs.alocar.domain.UseRepository;

@ExtendWith(MockitoExtension.class)
class UseServiceTest {

    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");
    private static final Tariff TARIFF = new Tariff(5, 20, Duration.ofMinutes(10), 10, 480);

    @Mock
    private UseRepository useRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentRepository paymentRepository;

    private Subfleet acacias;
    private Hub hubA;
    private Resident tiago;
    private Scooter scooter;

    @BeforeEach
    void setUp() {
        acacias = withId(new Subfleet("Residencial Quinta das Acácias"), 1L);
        hubA = withId(new Hub("Hub Bloco A", "A", 8, acacias, LocalTime.of(7, 0), LocalTime.of(23, 0)), 10L);
        tiago = withId(new Resident("Tiago Mendes", "tiago.mendes@exemplo.pt", acacias), 100L);
        scooter = withId(new Scooter("TR-001", hubA, ScooterStatus.AVAILABLE), 1000L);
        lenient().when(residentRepository.findById(100L)).thenReturn(Optional.of(tiago));
        lenient().when(scooterRepository.findById(1000L)).thenReturn(Optional.of(scooter));
        lenient().when(paymentGateway.charge(anyLong(), anyLong(), anyString())).thenReturn(true);
        lenient().when(useRepository.save(any(Use.class))).thenAnswer(call -> call.getArgument(0));
    }

    private UseService serviceAt(int hour, int minute) {
        return serviceAt(hour, minute, TARIFF);
    }

    private UseService serviceAt(int hour, int minute, Tariff tariff) {
        Instant now = ZonedDateTime.of(2026, 11, 23, hour, minute, 0, 0, LISBON).toInstant();
        return new UseService(
                useRepository,
                scooterRepository,
                residentRepository,
                paymentRepository,
                paymentGateway,
                tariff,
                Clock.fixed(now, LISBON));
    }

    @Test
    void startChargesTheChosenDurationAndPicksUpTheScooter() {
        Use use = serviceAt(8, 35).start(100L, 1000L, 240);

        assertThat(use.getAmountPaidCents()).isEqualTo(1200);
        assertThat(use.getExpectedEndAt()).isEqualTo(Instant.parse("2026-11-23T12:35:00Z"));
        assertThat(scooter.getStatus()).isEqualTo(ScooterStatus.IN_USE);
        verify(paymentGateway).charge(eq(100L), eq(1200L), anyString());
    }

    @Test
    void br2ScooterOfAnotherSubfleetIsRefused() {
        Subfleet salinas = withId(new Subfleet("Residência Salinas"), 2L);
        Hub otherHub = withId(new Hub("Hub Salinas", "S", 4, salinas, null, null), 20L);
        Scooter other = withId(new Scooter("TR-100", otherHub, ScooterStatus.AVAILABLE), 2000L);
        when(scooterRepository.findById(2000L)).thenReturn(Optional.of(other));

        assertRefused(() -> serviceAt(10, 0).start(100L, 2000L, 60), "BR2");
    }

    @Test
    void br3ScooterNotAvailableIsRefused() {
        ReflectionTestUtils.setField(scooter, "status", ScooterStatus.MAINTENANCE);

        assertRefused(() -> serviceAt(10, 0).start(100L, 1000L, 60), "BR3");
    }

    @Test
    void br3StartBeforeOpeningIsRefusedAndAtOpeningIsAccepted() {
        assertRefused(() -> serviceAt(6, 59).start(100L, 1000L, 60), "BR3");
        assertThat(serviceAt(7, 0).start(100L, 1000L, 60).isActive()).isTrue();
    }

    @Test
    void br4DurationOutsideTheLimitsIsRefused() {
        assertRefused(() -> serviceAt(10, 0).start(100L, 1000L, 9), "BR4");
        assertRefused(() -> serviceAt(10, 0).start(100L, 1000L, 481), "BR4");
    }

    @Test
    void br5UseMustEndBeforeTheHubCloses() {
        assertRefused(() -> serviceAt(22, 0).start(100L, 1000L, 61), "BR5");
        assertThat(serviceAt(22, 0).start(100L, 1000L, 60).isActive()).isTrue();
    }

    @Test
    void br6SecondActiveUseIsRefused() {
        when(useRepository.existsByResidentIdAndEndedAtIsNull(100L)).thenReturn(true);

        assertRefused(() -> serviceAt(10, 0).start(100L, 1000L, 60), "BR6");
    }

    @Test
    void declinedPaymentDoesNotStartTheUse() {
        when(paymentGateway.charge(anyLong(), anyLong(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> serviceAt(10, 0).start(100L, 1000L, 60))
                .isInstanceOf(PaymentDeclinedException.class);
        assertThat(scooter.isAvailable()).isTrue();
        verify(useRepository, never()).save(any());
    }

    @Test
    void unknownResidentOrScooterIsNotFound() {
        when(residentRepository.findById(9L)).thenReturn(Optional.empty());
        when(scooterRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceAt(10, 0).start(9L, 1000L, 60)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> serviceAt(10, 0).start(100L, 9L, 60)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void br8ReturnLessThan10MinutesLateHasNoFine() {
        Use use = activeUseStartedAt(8, 35, 240);

        Use ended = serviceAt(12, 44).returnScooter(5L, 10L);

        assertThat(ended).isSameAs(use);
        assertThat(ended.getFineCents()).isZero();
        assertThat(scooter.isAvailable()).isTrue();
    }

    @Test
    void br8Return14MinutesLatePaysFineFor14Minutes() {
        activeUseStartedAt(8, 35, 240);

        Use ended = serviceAt(12, 49).returnScooter(5L, 10L);

        assertThat(ended.getFineCents()).isEqualTo(14 * 20);
        assertThat(ended.isActive()).isFalse();
    }

    @Test
    void br7ReturnAtAnotherHubIsRefused() {
        activeUseStartedAt(8, 35, 240);

        assertRefused(() -> serviceAt(12, 0).returnScooter(5L, 99L), "BR7");
        assertThat(scooter.getStatus()).isEqualTo(ScooterStatus.IN_USE);
    }

    @Test
    void endedUseCannotBeReturnedAgain() {
        activeUseStartedAt(8, 35, 60);
        serviceAt(9, 30).returnScooter(5L, 10L);

        assertRefused(() -> serviceAt(9, 31).returnScooter(5L, 10L), "USE_ENDED");
    }

    @Test
    void historyOfUnknownResidentIsNotFound() {
        when(residentRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> serviceAt(10, 0).history(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void historyListsTheResidentsUses() {
        Use use = activeUseStartedAt(8, 35, 60);
        when(residentRepository.existsById(100L)).thenReturn(true);
        when(useRepository.findByResidentIdOrderByStartedAtDesc(100L)).thenReturn(List.of(use));

        assertThat(serviceAt(10, 0).history(100L)).containsExactly(use);
    }

    /** US06: the duration charged at pick-up shows in the resident's payments. */
    @Test
    void startRecordsThePaymentOfTheChosenDuration() {
        Use use = serviceAt(8, 35).start(100L, 1000L, 240);

        Payment payment = savedPayment();
        assertThat(payment.getUse()).isSameAs(use);
        assertThat(payment.getKind()).isEqualTo(PaymentKind.USE);
        assertThat(payment.getAmountCents()).isEqualTo(1200);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    /** US06 + BR8: a late return charges the fine and records it as a payment. */
    @Test
    void br8LateReturnChargesTheFineAndRecordsIt() {
        Use use = activeUseStartedAt(8, 35, 240);

        serviceAt(12, 49).returnScooter(5L, 10L);

        verify(paymentGateway).charge(eq(100L), eq(280L), anyString());
        Payment fine = savedPayment();
        assertThat(fine.getUse()).isSameAs(use);
        assertThat(fine.getKind()).isEqualTo(PaymentKind.FINE);
        assertThat(fine.getAmountCents()).isEqualTo(280);
        assertThat(fine.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(fine.getCreatedAt()).isEqualTo(use.getEndedAt());
    }

    /** BR8 boundary: exactly 10 min 00 s late already charges the fine for 10 minutes. */
    @Test
    void br8Return10MinutesLateChargesTheFine() {
        activeUseStartedAt(8, 35, 240);

        serviceAt(12, 45).returnScooter(5L, 10L);

        verify(paymentGateway).charge(eq(100L), eq(200L), anyString());
        assertThat(savedPayment().getAmountCents()).isEqualTo(200);
    }

    /** A payment service outage must not keep the use open (the fine would keep growing). */
    @Test
    void paymentServiceOutageEndsTheUseAndRecordsTheFineAsOwed() {
        Use use = activeUseStartedAt(8, 35, 240);
        when(paymentGateway.charge(anyLong(), anyLong(), anyString()))
                .thenThrow(new IllegalStateException("payment service unavailable"));

        serviceAt(12, 49).returnScooter(5L, 10L);

        assertThat(use.isActive()).isFalse();
        assertThat(scooter.isAvailable()).isTrue();
        assertThat(savedPayment().getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }

    /** Two returns of the same use at once: the use is read with a lock, so only one ends it and pays. */
    @Test
    void returnReadsTheUseWithALock() {
        activeUseStartedAt(8, 35, 240);

        serviceAt(12, 49).returnScooter(5L, 10L);

        verify(useRepository).findByIdForUpdate(5L);
        verify(useRepository, never()).findById(5L);
    }

    /** With a tariff of 0 €/min nothing is charged, so there is no payment to record. */
    @Test
    void freeUseRecordsNoPayment() {
        Tariff free = new Tariff(0, 20, Duration.ofMinutes(10), 10, 480);

        assertThat(serviceAt(8, 35, free).start(100L, 1000L, 60).isActive()).isTrue();

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void br8ReturnLessThan10MinutesLateChargesNothing() {
        activeUseStartedAt(8, 35, 240);

        serviceAt(12, 44).returnScooter(5L, 10L);

        verify(paymentGateway, never()).charge(anyLong(), anyLong(), anyString());
        verify(paymentRepository, never()).save(any());
    }

    /** A declined fine does not keep the scooter: the use ends and the fine stays owed. */
    @Test
    void declinedFineEndsTheUseAndIsRecordedAsDeclined() {
        Use use = activeUseStartedAt(8, 35, 240);
        when(paymentGateway.charge(anyLong(), anyLong(), anyString())).thenReturn(false);

        serviceAt(12, 49).returnScooter(5L, 10L);

        assertThat(use.isActive()).isFalse();
        assertThat(scooter.isAvailable()).isTrue();
        assertThat(savedPayment().getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    void paymentsOfUnknownResidentAreNotFound() {
        when(residentRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> serviceAt(10, 0).payments(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void paymentsListsTheResidentsPayments() {
        Use use = activeUseStartedAt(8, 35, 60);
        Payment payment = new Payment(use, PaymentKind.USE, 300, PaymentStatus.PAID, use.getStartedAt());
        when(residentRepository.existsById(100L)).thenReturn(true);
        when(paymentRepository.findByResidentIdOrderByCreatedAtDescIdDesc(100L)).thenReturn(List.of(payment));

        assertThat(serviceAt(10, 0).payments(100L)).containsExactly(payment);
    }

    private Payment savedPayment() {
        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        return payment.getValue();
    }

    private Use activeUseStartedAt(int hour, int minute, int durationMinutes) {
        scooter.pickUp();
        Instant start = ZonedDateTime.of(2026, 11, 23, hour, minute, 0, 0, LISBON).toInstant();
        Use use = withId(new Use(tiago, scooter, start, durationMinutes, durationMinutes * 5L), 5L);
        lenient().when(useRepository.findById(5L)).thenReturn(Optional.of(use));
        lenient().when(useRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(use));
        return use;
    }

    private static void assertRefused(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, String rule) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessRuleException.class)
                .extracting(e -> ((BusinessRuleException) e).getRule())
                .isEqualTo(rule);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
