package mine.hospital_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mine.hospital_service.model.Hospital;
import mine.hospital_service.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HospitalController.class)
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HospitalService hospitalService;

    @Autowired
    private ObjectMapper objectMapper;

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
        hospital.setAmbulanceIds(new ArrayList<>());
    }

    @Test
    void getAllHospitals_ShouldReturnListOfHospitals() throws Exception {
        when(hospitalService.getAllHospitals()).thenReturn(Arrays.asList(hospital));

        mockMvc.perform(get("/hospitals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Hospital"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getHospitalById_WhenExists_ShouldReturnHospital() throws Exception {
        when(hospitalService.getHospitalById(1L)).thenReturn(Optional.of(hospital));

        mockMvc.perform(get("/hospitals/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hospital"));
    }

    @Test
    void getHospitalById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(hospitalService.getHospitalById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/hospitals/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createHospitals_ShouldReturnCreatedHospitals() throws Exception {
        List<Hospital> hospitals = Arrays.asList(hospital);
        when(hospitalService.createHospital(any(Hospital.class))).thenReturn(hospital);

        mockMvc.perform(post("/hospitals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hospitals)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Hospital"));
    }

    @Test
    void updateHospital_WhenExists_ShouldReturnUpdatedHospital() throws Exception {
        Hospital updatedHospital = new Hospital();
        updatedHospital.setName("Updated Hospital");
        updatedHospital.setAvailable(false);

        when(hospitalService.updateHospital(eq(1L), any(Hospital.class)))
                .thenReturn(Optional.of(updatedHospital));

        mockMvc.perform(put("/hospitals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedHospital)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hospital"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateHospital_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(hospitalService.updateHospital(eq(99L), any(Hospital.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/hospitals/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Hospital())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHospital_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/hospitals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addAmbulanceToHospital_WhenSuccess_ShouldReturnOk() throws Exception {
        when(hospitalService.addAmbulanceToHospital(1L, 1))
                .thenReturn(Optional.of(hospital));

        mockMvc.perform(post("/hospitals/1/ambulances/1"))
                .andExpect(status().isOk());
    }

    @Test
    void removeAmbulanceFromHospital_WhenSuccess_ShouldReturnOk() throws Exception {
        when(hospitalService.removeAmbulanceFromHospital(1L, 1))
                .thenReturn(Optional.of(hospital));

        mockMvc.perform(delete("/hospitals/1/ambulances/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllSpecialities_ShouldReturnSetOfSpecialities() throws Exception {
        Set<String> specialities = new HashSet<>(Arrays.asList("General", "Cardiology"));
        when(hospitalService.getAllSpecialities()).thenReturn(specialities);

        mockMvc.perform(get("/hospitals/specialities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[1]").exists());
    }

    @Test
    void getHospitalsBySpeciality_ShouldReturnFilteredHospitals() throws Exception {
        when(hospitalService.findBySpeciality("General"))
                .thenReturn(Arrays.asList(hospital));

        mockMvc.perform(get("/hospitals/search")
                .param("speciality", "General"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].speciality").value("General"));
    }

    @Test
    void getAmbulancesByHospital_WhenHospitalExists_ShouldReturnAmbulances() throws Exception {
        Map<Integer, Object> ambulanceDetails = new HashMap<>();
        ambulanceDetails.put(1, new HashMap<String, Object>());
        when(hospitalService.findByAmbulanceIds(1L)).thenReturn(ambulanceDetails);

        mockMvc.perform(get("/hospitals/1/ambulances"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
