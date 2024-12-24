package mine.hospital_service.service;

import mine.hospital_service.model.Hospital;
import mine.hospital_service.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private HospitalService hospitalService;

    private Hospital hospital;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        hospital = new Hospital();
        hospital.setId(1);
        hospital.setName("Test Hospital");
        hospital.setLatitude(40.7128);
        hospital.setLongitude(-74.0060);
        hospital.setAddress("123 Test St");
        hospital.setSpeciality("General");
        hospital.setAvailable(true);
        hospital.setAmbulanceIds(new ArrayList<>());
    }

    @Test
    void getAllHospitals_ShouldReturnListOfHospitals() {
        // Arrange
        List<Hospital> expectedHospitals = Arrays.asList(hospital);
        when(hospitalRepository.findAll()).thenReturn(expectedHospitals);

        // Act
        List<Hospital> actualHospitals = hospitalService.getAllHospitals();

        // Assert
        assertEquals(expectedHospitals, actualHospitals);
        verify(hospitalRepository).findAll();
    }

    @Test
    void getHospitalById_WhenExists_ShouldReturnHospital() {
        // Arrange
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        // Act
        Optional<Hospital> result = hospitalService.getHospitalById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(hospital, result.get());
    }

    @Test
    void getHospitalById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Hospital> result = hospitalService.getHospitalById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void createHospital_ShouldReturnSavedHospital() {
        // Arrange
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        // Act
        Hospital savedHospital = hospitalService.createHospital(hospital);

        // Assert
        assertNotNull(savedHospital);
        assertEquals(hospital, savedHospital);
        verify(hospitalRepository).save(hospital);
    }

    @Test
    void updateHospital_WhenExists_ShouldReturnUpdatedHospital() {
        // Arrange
        Hospital updatedHospital = new Hospital();
        updatedHospital.setName("Updated Hospital");
        updatedHospital.setAvailable(false);
        
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(updatedHospital);

        // Act
        Optional<Hospital> result = hospitalService.updateHospital(1L, updatedHospital);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Hospital", result.get().getName());
        assertFalse(result.get().isAvailable());
    }

    @Test
    void updateHospital_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(hospitalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Hospital> result = hospitalService.updateHospital(99L, new Hospital());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void deleteHospital_ShouldCallRepositoryDelete() {
        // Arrange
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        // Act
        hospitalService.deleteHospital(1L);

        // Assert
        verify(hospitalRepository).delete(hospital);
    }

    @Test
    void addAmbulanceToHospital_WhenHospitalExists_ShouldAddAmbulance() {
        // Arrange
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        // Mock WebClient behavior
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(new HashMap<>()));

        // Act
        Optional<Hospital> result = hospitalService.addAmbulanceToHospital(1L, 1);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().getAmbulanceIds().contains(1));
    }

    @Test
    void removeAmbulanceFromHospital_WhenHospitalExists_ShouldRemoveAmbulance() {
        // Arrange
        hospital.getAmbulanceIds().add(1);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        // Act
        Optional<Hospital> result = hospitalService.removeAmbulanceFromHospital(1L, 1);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().getAmbulanceIds().contains(1));
    }

    @Test
    void getAllSpecialities_ShouldReturnUniqueSpecialities() {
        // Arrange
        List<Hospital> hospitals = Arrays.asList(
            createHospitalWithSpeciality("Cardiology"),
            createHospitalWithSpeciality("Neurology"),
            createHospitalWithSpeciality("Cardiology")
        );
        when(hospitalRepository.findAll()).thenReturn(hospitals);

        // Act
        Set<String> specialities = hospitalService.getAllSpecialities();

        // Assert
        assertEquals(2, specialities.size());
        assertTrue(specialities.contains("Cardiology"));
        assertTrue(specialities.contains("Neurology"));
    }

    private Hospital createHospitalWithSpeciality(String speciality) {
        Hospital hospital = new Hospital();
        hospital.setSpeciality(speciality);
        return hospital;
    }
}
