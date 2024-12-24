package mine.dispatchcoordinationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mine.dispatchcoordinationservice.dto.*;
import mine.dispatchcoordinationservice.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DispatchController.class)
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DispatchService dispatchService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmergencyRequest emergencyRequest;
    private DispatchResult successResult;
    private DispatchResult failureResult;
    private Hospital hospital;
    private Ambulance ambulance;

    @BeforeEach
    void setUp() {
        // Setup test data
        emergencyRequest = new EmergencyRequest();
        emergencyRequest.setLatitude(40.7589);
        emergencyRequest.setLongitude(-73.9851);
        emergencyRequest.setSpecialization("Cardiology");

        hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("Test Hospital");
        hospital.setSpecialization("Cardiology");

        ambulance = new Ambulance();
        ambulance.setId(1);
        ambulance.setAvailable(true);
        ambulance.setLatitude(40.7128);
        ambulance.setLongitude(-74.0060);

        // Setup success result
        successResult = new DispatchResult();
        successResult.setStatus("SUCCESS");
        successResult.setAssignedHospital(hospital);
        successResult.setAssignedAmbulance(ambulance);
        successResult.setRoutePolyline("test_geometry");

        // Setup failure result
        failureResult = new DispatchResult();
        failureResult.setStatus("FAILURE");
    }

    @Test
    void handleEmergency_Success() throws Exception {
        when(dispatchService.handleEmergency(any(EmergencyRequest.class)))
                .thenReturn(successResult);

        mockMvc.perform(post("/dispatch/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emergencyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.assignedHospital.id").value(1))
                .andExpect(jsonPath("$.assignedAmbulance.id").value(1))
                .andExpect(jsonPath("$.routePolyline").value("test_geometry"));
    }

    @Test
    void handleEmergency_Failure() throws Exception {
        when(dispatchService.handleEmergency(any(EmergencyRequest.class)))
                .thenReturn(failureResult);

        mockMvc.perform(post("/dispatch/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emergencyRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.assignedHospital").doesNotExist())
                .andExpect(jsonPath("$.assignedAmbulance").doesNotExist());
    }

    @Test
    void handleEmergency_InvalidRequest() throws Exception {
        EmergencyRequest invalidRequest = new EmergencyRequest();
        // Missing required fields

        mockMvc.perform(post("/dispatch/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
