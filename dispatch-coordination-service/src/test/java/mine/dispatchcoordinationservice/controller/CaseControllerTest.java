package mine.dispatchcoordinationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mine.dispatchcoordinationservice.model.Case;
import mine.dispatchcoordinationservice.service.CaseService;
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

@WebMvcTest(CaseController.class)
class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseService caseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Case testCase;

    @BeforeEach
    void setUp() {
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
    void getAllCases_ShouldReturnListOfCases() throws Exception {
        when(caseService.getAllCases()).thenReturn(Arrays.asList(testCase));

        mockMvc.perform(get("/cases"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void getCaseById_WhenExists_ShouldReturnCase() throws Exception {
        when(caseService.getCaseById(1)).thenReturn(Optional.of(testCase));

        mockMvc.perform(get("/cases/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getCaseById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(caseService.getCaseById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cases/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCase_WhenExists_ShouldReturnUpdatedCase() throws Exception {
        Case updatedCase = new Case();
        updatedCase.setId(1);
        updatedCase.setStatus("COMPLETED");
        updatedCase.setRealDuration(550.0);

        when(caseService.getCaseById(1)).thenReturn(Optional.of(testCase));
        when(caseService.updateCase(any(Case.class))).thenReturn(updatedCase);

        mockMvc.perform(put("/cases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.realDuration").value(550.0));
    }

    @Test
    void updateCase_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(caseService.getCaseById(99)).thenReturn(Optional.empty());

        Case updatedCase = new Case();
        updatedCase.setId(99);
        updatedCase.setStatus("COMPLETED");

        mockMvc.perform(put("/cases/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCase)))
                .andExpect(status().isNotFound());
    }
}
