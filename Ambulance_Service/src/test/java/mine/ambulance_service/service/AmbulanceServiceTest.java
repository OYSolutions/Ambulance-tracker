package mine.ambulance_service.service;

import mine.ambulance_service.model.Ambulance;
import mine.ambulance_service.repository.AmbulanceRepository;
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

class AmbulanceServiceTest {

    @Mock
    private AmbulanceRepository ambulanceRepository;

    @InjectMocks
    private AmbulanceService ambulanceService;

    private Ambulance ambulance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ambulance = new Ambulance();
        ambulance.setId(1L);
        ambulance.setDriverName("John Doe");
        ambulance.setAvailable(true);
        ambulance.setLatitude(40.7128);
        ambulance.setLongitude(-74.0060);
    }

    @Test
    void getAllAmbulances_ShouldReturnListOfAmbulances() {
        // Arrange
        List<Ambulance> expectedAmbulances = Arrays.asList(ambulance);
        when(ambulanceRepository.findAll()).thenReturn(expectedAmbulances);

        // Act
        List<Ambulance> actualAmbulances = ambulanceService.getAllAmbulances();

        // Assert
        assertEquals(expectedAmbulances, actualAmbulances);
        verify(ambulanceRepository).findAll();
    }

    @Test
    void getAmbulanceById_WhenExists_ShouldReturnAmbulance() {
        // Arrange
        when(ambulanceRepository.findById(1)).thenReturn(Optional.of(ambulance));

        // Act
        Optional<Ambulance> result = ambulanceService.getAmbulanceById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ambulance, result.get());
    }

    @Test
    void getAmbulanceById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(ambulanceRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        Optional<Ambulance> result = ambulanceService.getAmbulanceById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void createAmbulance_ShouldReturnSavedAmbulance() {
        // Arrange
        when(ambulanceRepository.save(any(Ambulance.class))).thenReturn(ambulance);

        // Act
        Ambulance savedAmbulance = ambulanceService.createAmbulance(ambulance);

        // Assert
        assertNotNull(savedAmbulance);
        assertEquals(ambulance, savedAmbulance);
        verify(ambulanceRepository).save(ambulance);
    }

    @Test
    void updateAmbulance_WhenExists_ShouldReturnUpdatedAmbulance() {
        // Arrange
        Ambulance updatedAmbulance = new Ambulance();
        updatedAmbulance.setAvailable(false);
        updatedAmbulance.setLatitude(41.8781);
        updatedAmbulance.setLongitude(-87.6298);
        
        when(ambulanceRepository.findById(1)).thenReturn(Optional.of(ambulance));
        when(ambulanceRepository.save(any(Ambulance.class))).thenReturn(ambulance);

        // Act
        Optional<Ambulance> result = ambulanceService.updateAmbulance(1L, updatedAmbulance);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(false, result.get().isAvailable());
        assertEquals(41.8781, result.get().getLatitude());
        assertEquals(-87.6298, result.get().getLongitude());
    }

    @Test
    void updateAmbulance_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(ambulanceRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        Optional<Ambulance> result = ambulanceService.updateAmbulance(99L, new Ambulance());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void updateAmbulanceLocation_WhenExists_ShouldReturnUpdatedAmbulance() {
        // Arrange
        Double newLatitude = 41.8781;
        Double newLongitude = -87.6298;
        
        when(ambulanceRepository.findById(1)).thenReturn(Optional.of(ambulance));
        when(ambulanceRepository.save(any(Ambulance.class))).thenReturn(ambulance);

        // Act
        Optional<Ambulance> result = ambulanceService.updateAmbulanceLocation(1L, newLatitude, newLongitude);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(newLatitude, result.get().getLatitude());
        assertEquals(newLongitude, result.get().getLongitude());
    }

    @Test
    void updateAmbulanceAvailability_WhenExists_ShouldReturnUpdatedAmbulance() {
        // Arrange
        when(ambulanceRepository.findById(1)).thenReturn(Optional.of(ambulance));
        when(ambulanceRepository.save(any(Ambulance.class))).thenReturn(ambulance);

        // Act
        Optional<Ambulance> result = ambulanceService.updateAmbulanceAvailability(1L, false);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().isAvailable());
    }

    @Test
    void deleteAmbulance_ShouldCallRepositoryDelete() {
        // Arrange
        when(ambulanceRepository.findById(1)).thenReturn(Optional.of(ambulance));

        // Act
        ambulanceService.deleteAmbulance(1L);

        // Assert
        verify(ambulanceRepository).delete(ambulance);
    }
}
