package mine.dispatchcoordinationservice.service;

import mine.dispatchcoordinationservice.model.Case;
import mine.dispatchcoordinationservice.repository.CaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public Case createCase(Case newCase) {
        return caseRepository.save(newCase);
    }

    public Optional<Case> getCaseById(Integer id) {
        return caseRepository.findById(id);
    }

    public List<Case> getAllCases() {
        return caseRepository.findAll();
    }

    public Case updateCase(Case updatedCase) {
        return caseRepository.save(updatedCase);
    }
}