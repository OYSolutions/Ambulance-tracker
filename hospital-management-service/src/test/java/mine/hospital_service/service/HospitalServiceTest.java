package mine.hospital_service.service;

import mine.hospital_service.dto.AmbulanceDTO;
import mine.hospital_service.model.Hospital;
import mine.hospital_service.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

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
        hospital = new Hospital();
        hospital.setId(1);
        hospital.setName("Test Hospital");
        hospital.setLatitude(40.7128);
        hospital.setLongitude(-74.0060);
        hospital.setAddress("123 Test St");
        hospital.setSpeciality("General");
        hospital.setAvailable(true);
        hospital.setAmbulanceIds(Arrays.asList(1, 2));

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getAllHospitals_ShouldReturnAllHospitals() {
        when(hospitalRepository.findAll()).thenReturn(Arrays.asList(hospital));

        List<Hospital> result = hospitalService.getAllHospitals();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Hospital", result.get(0).getName());
    }

    @Test
    void getHospitalById_WhenExists_ShouldReturnHospital() {
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));

        Optional<Hospital> result = hospitalService.getHospitalById(1);

        assertTrue(result.isPresent());
        assertEquals("Test Hospital", result.get().getName());
    }

    @Test
    void getHospitalById_WhenNotExists_ShouldReturnEmpty() {
        when(hospitalRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Hospital> result = hospitalService.getHospitalById(99);

        assertFalse(result.isPresent());
    }

    @Test
    void createHospital_ShouldReturnCreatedHospital() {
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        Hospital result = hospitalService.createHospital(hospital);

        assertNotNull(result);
        assertEquals("Test Hospital", result.getName());
        assertEquals("General", result.getSpeciality());
        assertTrue(result.isAvailable());
    }

    @Test
    void updateHospital_WhenExists_ShouldReturnUpdatedHospital() {
        Hospital updatedHospital = new Hospital();
        updatedHospital.setId(1);
        updatedHospital.setName("Updated Hospital");
        updatedHospital.setLatitude(40.7128);
        updatedHospital.setLongitude(-74.0060);
        updatedHospital.setAddress("456 Updated St");
        updatedHospital.setSpeciality("Emergency");
        updatedHospital.setAvailable(false);
        updatedHospital.setAmbulanceIds(Arrays.asList(3, 4));

        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(updatedHospital);

        Optional<Hospital> result = hospitalService.updateHospital(1, updatedHospital);

        assertTrue(result.isPresent());
        assertEquals("Updated Hospital", result.get().getName());
        assertEquals("Emergency", result.get().getSpeciality());
        assertFalse(result.get().isAvailable());
    }

    @Test
    void updateHospital_WhenNotExists_ShouldReturnEmpty() {
        when(hospitalRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Hospital> result = hospitalService.updateHospital(99, new Hospital());

        assertFalse(result.isPresent());
    }

    @Test
    void deleteHospital_WhenExists_ShouldReturnTrue() {
        when(hospitalRepository.existsById(1)).thenReturn(true);
        doNothing().when(hospitalRepository).deleteById(1);

        boolean result = hospitalService.deleteHospital(1);

        assertTrue(result);
        verify(hospitalRepository).deleteById(1);
    }

    @Test
    void deleteHospital_WhenNotExists_ShouldReturnFalse() {
        when(hospitalRepository.existsById(99)).thenReturn(false);

        boolean result = hospitalService.deleteHospital(99);

        assertFalse(result);
        verify(hospitalRepository, never()).deleteById(anyInt());
    }

    @Test
    void addAmbulanceToHospital_WhenSuccess_ShouldReturnUpdatedHospital() {
        Hospital existingHospital = new Hospital();
        existingHospital.setId(1);
        existingHospital.setAmbulanceIds(new ArrayList<>());

        when(hospitalRepository.findById(1)).thenReturn(Optional.of(existingHospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        Optional<Hospital> result = hospitalService.addAmbulanceToHospital(1, 1);

        assertTrue(result.isPresent());
        assertTrue(result.get().getAmbulanceIds().contains(1));
    }

    @Test
    void removeAmbulanceFromHospital_WhenSuccess_ShouldReturnUpdatedHospital() {
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);

        Optional<Hospital> result = hospitalService.removeAmbulanceFromHospital(1, 1);

        assertTrue(result.isPresent());
        assertFalse(result.get().getAmbulanceIds().contains(1));
    }

    @Test
    void getAllSpecialities_ShouldReturnUniqueSpecialities() {
        Hospital hospital2 = new Hospital();
        hospital2.setSpeciality("Emergency");

        when(hospitalRepository.findAll()).thenReturn(Arrays.asList(hospital, hospital2));

        Set<String> result = hospitalService.getAllSpecialities();

        assertEquals(2, result.size());
        assertTrue(result.contains("General"));
        assertTrue(result.contains("Emergency"));
    }

    @Test
    void findBySpeciality_ShouldReturnFilteredHospitals() {
        when(hospitalRepository.findBySpeciality("General")).thenReturn(Arrays.asList(hospital));

        List<Hospital> result = hospitalService.findBySpeciality("General");

        assertEquals(1, result.size());
        assertEquals("General", result.get(0).getSpeciality());
    }

    @Test
    void findByAmbulanceIds_WhenAmbulanceExists_ShouldReturnAmbulanceDetails() {
        AmbulanceDTO ambulance = new AmbulanceDTO();
        ambulance.setId(1);
        ambulance.setDriverName("John Doe");
        ambulance.setAvailable(true);

        when(responseSpec.bodyToMono(AmbulanceDTO.class))
                .thenReturn(Mono.just(ambulance));

        Map<Integer, Object> result = hospitalService.findByAmbulanceIds(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(2));
    }
}
