package mine.dispatchcoordinationservice.service;

import mine.dispatchcoordinationservice.model.Case;
import mine.dispatchcoordinationservice.repository.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private CaseService caseService;

    private Case testCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCase = new Case();
        testCase.setId(1);
        testCase.setLatitude(40.7589);
        testCase.setLongitude(-73.9851);
        testCase.setSpecialization("Cardiology");
        testCase.setStatus("IN_PROGRESS");
        testCase.setAssignedAmbulanceId(1);
        testCase.setAssignedHospitalId(1L);
        testCase.setEstimatedDuration(600.0);
        testCase.setEstimatedDistance(1000.0);
        testCase.setRouteGeometry("test_geometry");
    }

    @Test
    void createCase_ShouldReturnSavedCase() {
        // Arrange
        when(caseRepository.save(any(Case.class))).thenReturn(testCase);

        // Act
        Case savedCase = caseService.createCase(testCase);

        // Assert
        assertNotNull(savedCase);
        assertEquals(testCase.getId(), savedCase.getId());
        assertEquals(testCase.getSpecialization(), savedCase.getSpecialization());
        assertEquals(testCase.getStatus(), savedCase.getStatus());
        verify(caseRepository).save(testCase);
    }

    @Test
    void getCaseById_WhenExists_ShouldReturnCase() {
        // Arrange
        when(caseRepository.findById(1)).thenReturn(Optional.of(testCase));

        // Act
        Optional<Case> result = caseService.getCaseById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCase, result.get());
    }

    @Test
    void getCaseById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(caseRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        Optional<Case> result = caseService.getCaseById(99);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getAllCases_ShouldReturnListOfCases() {
        // Arrange
        List<Case> expectedCases = Arrays.asList(testCase);
        when(caseRepository.findAll()).thenReturn(expectedCases);

        // Act
        List<Case> actualCases = caseService.getAllCases();

        // Assert
        assertEquals(expectedCases.size(), actualCases.size());
        assertEquals(expectedCases.get(0), actualCases.get(0));
        verify(caseRepository).findAll();
    }

    @Test
    void updateCase_ShouldReturnUpdatedCase() {
        // Arrange
        Case updatedCase = new Case();
        updatedCase.setId(1);
        updatedCase.setStatus("COMPLETED");
        updatedCase.setRealDuration(550.0);
        
        when(caseRepository.save(any(Case.class))).thenReturn(updatedCase);

        // Act
        Case result = caseService.updateCase(updatedCase);

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(550.0, result.getRealDuration());
        verify(caseRepository).save(updatedCase);
    }
}
