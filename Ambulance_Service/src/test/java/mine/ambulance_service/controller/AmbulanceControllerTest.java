package mine.ambulance_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mine.ambulance_service.model.Ambulance;
import mine.ambulance_service.service.AmbulanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AmbulanceController.class)
class AmbulanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmbulanceService ambulanceService;

    @Autowired
    private ObjectMapper objectMapper;

    private Ambulance ambulance;

    @BeforeEach
    void setUp() {
        ambulance = new Ambulance();
        ambulance.setId(1L);
        ambulance.setDriverName("John Doe");
        ambulance.setLatitude(40.7128);
        ambulance.setLongitude(-74.0060);
        ambulance.setAvailable(true);
    }

    @Test
    void getAllAmbulances_ShouldReturnListOfAmbulances() throws Exception {
        when(ambulanceService.getAllAmbulances()).thenReturn(Arrays.asList(ambulance));

        mockMvc.perform(get("/api/ambulances"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].driverName").value("John Doe"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getAmbulanceById_WhenExists_ShouldReturnAmbulance() throws Exception {
        when(ambulanceService.getAmbulanceById(1L)).thenReturn(Optional.of(ambulance));

        mockMvc.perform(get("/api/ambulances/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverName").value("John Doe"));
    }

    @Test
    void getAmbulanceById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(ambulanceService.getAmbulanceById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ambulances/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAmbulance_ShouldReturnCreatedAmbulance() throws Exception {
        when(ambulanceService.createAmbulance(any(Ambulance.class))).thenReturn(ambulance);

        mockMvc.perform(post("/api/ambulances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ambulance)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverName").value("John Doe"));
    }

    @Test
    void updateAmbulance_WhenExists_ShouldReturnUpdatedAmbulance() throws Exception {
        Ambulance updatedAmbulance = new Ambulance();
        updatedAmbulance.setId(1L);
        updatedAmbulance.setDriverName("Jane Doe");
        updatedAmbulance.setAvailable(false);
        updatedAmbulance.setLatitude(41.8781);
        updatedAmbulance.setLongitude(-87.6298);

        when(ambulanceService.updateAmbulance(eq(1L), any(Ambulance.class)))
                .thenReturn(Optional.of(updatedAmbulance));

        mockMvc.perform(put("/api/ambulances/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAmbulance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverName").value("Jane Doe"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.latitude").value(41.8781))
                .andExpect(jsonPath("$.longitude").value(-87.6298));
    }

    @Test
    void updateAmbulance_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(ambulanceService.updateAmbulance(eq(99L), any(Ambulance.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/ambulances/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Ambulance())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAmbulanceLocation_WhenExists_ShouldReturnUpdatedAmbulance() throws Exception {
        Ambulance locationUpdate = new Ambulance();
        locationUpdate.setLatitude(41.8781);
        locationUpdate.setLongitude(-87.6298);

        when(ambulanceService.updateAmbulanceLocation(eq(1L), any(Double.class), any(Double.class)))
                .thenReturn(Optional.of(locationUpdate));

        mockMvc.perform(patch("/api/ambulances/1/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(41.8781))
                .andExpect(jsonPath("$.longitude").value(-87.6298));
    }

    @Test
    void updateAmbulanceLocation_WhenNotExists_ShouldReturnNotFound() throws Exception {
        Ambulance locationUpdate = new Ambulance();
        locationUpdate.setLatitude(41.8781);
        locationUpdate.setLongitude(-87.6298);

        when(ambulanceService.updateAmbulanceLocation(eq(1L), any(Double.class), any(Double.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/ambulances/1/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAmbulanceAvailability_WhenExists_ShouldReturnUpdatedAmbulance() throws Exception {
        Ambulance availabilityUpdate = new Ambulance();
        availabilityUpdate.setAvailable(false);

        when(ambulanceService.updateAmbulanceAvailability(eq(1L), eq(false)))
                .thenReturn(Optional.of(availabilityUpdate));

        mockMvc.perform(patch("/api/ambulances/1/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availabilityUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateAmbulanceAvailability_WhenNotExists_ShouldReturnNotFound() throws Exception {
        Ambulance availabilityUpdate = new Ambulance();
        availabilityUpdate.setAvailable(false);

        when(ambulanceService.updateAmbulanceAvailability(eq(1L), eq(false)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/ambulances/1/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availabilityUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAmbulance_WhenExists_ShouldReturnNoContent() throws Exception {
        when(ambulanceService.deleteAmbulance(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/ambulances/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAmbulance_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(ambulanceService.deleteAmbulance(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/ambulances/1"))
                .andExpect(status().isNotFound());
    }
}
