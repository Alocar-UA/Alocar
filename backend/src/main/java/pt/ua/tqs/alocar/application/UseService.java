package pt.ua.tqs.alocar.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pt.ua.tqs.alocar.domain.Tariff;
import pt.ua.tqs.alocar.domain.Use;
import pt.ua.tqs.alocar.domain.UseRepository;

/** Use cases UC2 (start a use), UC3 (return the scooter) and UC4 (history). */
@Service
@Transactional
public class UseService {

    private final UseRepository useRepository;
    private final ScooterRepository scooterRepository;
    private final ResidentRepository residentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final Tariff tariff;
    private final Clock clock;

    public UseService(
            UseRepository useRepository,
            ScooterRepository scooterRepository,
            ResidentRepository residentRepository,
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            Tariff tariff,
            Clock clock) {
        this.useRepository = useRepository;
        this.scooterRepository = scooterRepository;
        this.residentRepository = residentRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.tariff = tariff;
        this.clock = clock;
    }

    /** UC2 / US03: the resident picks a free scooter at a hub of their subfleet and pays the chosen time. */
    public Use start(Long residentId, Long scooterId, int durationMinutes) {
        Resident resident = residentRepository
                .findById(residentId)
                .orElseThrow(() -> new NotFoundException("Resident " + residentId + " not found"));
        Scooter scooter = scooterRepository
                .findById(scooterId)
                .orElseThrow(() -> new NotFoundException("Scooter " + scooterId + " not found"));
        Hub hub = scooter.getHub();

        if (!tariff.allowsDuration(durationMinutes)) {
            throw new BusinessRuleException(
                    "BR4",
                    "A duração tem de estar entre " + tariff.minDurationMinutes() + " e "
                            + tariff.maxDurationMinutes() + " minutos");
        }
        if (!Objects.equals(hub.getSubfleet().getId(), resident.getSubfleet().getId())) {
            throw new BusinessRuleException("BR2", "A trotinete não pertence à subfrota do residente");
        }
        if (!scooter.isAvailable()) {
            throw new BusinessRuleException("BR3", "A trotinete não está disponível");
        }
        LocalDateTime start = LocalDateTime.now(clock);
        if (!hub.isOpenAt(start.toLocalTime())) {
            throw new BusinessRuleException("BR3", "O hub está fechado");
        }
        if (!hub.coversPeriod(start, start.plusMinutes(durationMinutes))) {
            throw new BusinessRuleException("BR5", "A utilização tem de terminar antes de o hub fechar");
        }
        if (useRepository.existsByResidentIdAndEndedAtIsNull(residentId)) {
            throw new BusinessRuleException("BR6", "O residente já tem uma utilização ativa");
        }

        long price = tariff.priceFor(durationMinutes);
        if (!paymentGateway.charge(residentId, price, "Utilização da trotinete " + scooter.getCode())) {
            throw new PaymentDeclinedException("Pagamento recusado");
        }
        scooter.pickUp();
        Use use = useRepository.save(new Use(resident, scooter, clock.instant(), durationMinutes, price));
        paymentRepository.save(new Payment(use, PaymentKind.USE, price, PaymentStatus.PAID, use.getStartedAt()));
        return use;
    }

    /** UC3 / US04: the resident returns the scooter at the pick-up hub; a fine applies from 10 min late. */
    public Use returnScooter(Long useId, Long hubId) {
        Use use = get(useId);
        if (!use.isActive()) {
            throw new BusinessRuleException("USE_ENDED", "A utilização já terminou");
        }
        if (!Objects.equals(use.getHub().getId(), hubId)) {
            throw new BusinessRuleException("BR7", "A trotinete tem de ser devolvida no hub onde foi levantada");
        }
        Instant now = clock.instant();
        Duration delay = use.delayAt(now);
        long fine = tariff.fineFor(delay);
        use.getScooter().giveBack();
        use.end(now, fine);
        if (fine > 0) {
            boolean paid = paymentGateway.charge(
                    use.getResident().getId(), fine, "Coima da trotinete " + use.getScooter().getCode());
            paymentRepository.save(
                    new Payment(use, PaymentKind.FINE, fine, paid ? PaymentStatus.PAID : PaymentStatus.DECLINED, now));
        }
        return use;
    }

    @Transactional(readOnly = true)
    public Use get(Long useId) {
        return useRepository.findById(useId).orElseThrow(() -> new NotFoundException("Use " + useId + " not found"));
    }

    /** UC4 / US06: current and previous uses, most recent first. */
    @Transactional(readOnly = true)
    public List<Use> history(Long residentId) {
        if (!residentRepository.existsById(residentId)) {
            throw new NotFoundException("Resident " + residentId + " not found");
        }
        return useRepository.findByResidentIdOrderByStartedAtDesc(residentId);
    }

    /** UC4 / US06: the resident's payments (durations and fines), most recent first. */
    @Transactional(readOnly = true)
    public List<Payment> payments(Long residentId) {
        if (!residentRepository.existsById(residentId)) {
            throw new NotFoundException("Resident " + residentId + " not found");
        }
        return paymentRepository.findByResidentIdOrderByCreatedAtDescIdDesc(residentId);
    }
}
