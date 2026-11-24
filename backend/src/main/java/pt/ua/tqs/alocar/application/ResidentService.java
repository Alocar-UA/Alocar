package pt.ua.tqs.alocar.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.tqs.alocar.domain.Resident;
import pt.ua.tqs.alocar.domain.ResidentRepository;
import pt.ua.tqs.alocar.domain.Subfleet;
import pt.ua.tqs.alocar.domain.SubfleetRepository;

/** Use case UC1 (create an account through the residence's join link). */
@Service
@Transactional
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final SubfleetRepository subfleetRepository;

    public ResidentService(ResidentRepository residentRepository, SubfleetRepository subfleetRepository) {
        this.residentRepository = residentRepository;
        this.subfleetRepository = subfleetRepository;
    }

    /** UC1 / US01: the new resident belongs to the subfleet of the join link (BR2). */
    public Resident signUp(String joinToken, String name, String email) {
        Subfleet subfleet = subfleetRepository
                .findByJoinToken(joinToken)
                .orElseThrow(() -> new NotFoundException("Link de adesão inválido"));
        if (!subfleet.isJoinLinkActive()) {
            throw new JoinLinkDisabledException("O link de adesão foi desativado");
        }
        Resident resident = new Resident(name, email, subfleet);
        if (residentRepository.existsByEmail(resident.getEmail())) {
            throw new BusinessRuleException("BR2", "Já existe uma conta com este email");
        }
        return residentRepository.save(resident);
    }
}
