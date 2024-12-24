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
import static org.hamcrest.Matchers.containsInAnyOrder;

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
        hospital.setAmbulanceIds(Arrays.asList(1, 2));
    }

    @Test
    void getAllHospitals_ShouldReturnListOfHospitals() throws Exception {
        when(hospitalService.getAllHospitals()).thenReturn(Arrays.asList(hospital));

        mockMvc.perform(get("/hospitals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Hospital"))
                .andExpect(jsonPath("$[0].speciality").value("General"))
                .andExpect(jsonPath("$[0].ambulanceIds").isArray());
    }

    @Test
    void getHospitalById_WhenExists_ShouldReturnHospital() throws Exception {
        when(hospitalService.getHospitalById(1)).thenReturn(Optional.of(hospital));

        mockMvc.perform(get("/hospitals/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Hospital"))
                .andExpect(jsonPath("$.speciality").value("General"))
                .andExpect(jsonPath("$.ambulanceIds").isArray());
    }

    @Test
    void getHospitalById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(hospitalService.getHospitalById(99)).thenReturn(Optional.empty());

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
                .andExpect(jsonPath("$[0].name").value("Test Hospital"))
                .andExpect(jsonPath("$[0].speciality").value("General"))
                .andExpect(jsonPath("$[0].ambulanceIds").isArray());
    }

    @Test
    void updateHospital_WhenExists_ShouldReturnUpdatedHospital() throws Exception {
        Hospital updatedHospital = new Hospital();
        updatedHospital.setId(1);
        updatedHospital.setName("Updated Hospital");
        updatedHospital.setLatitude(40.7128);
        updatedHospital.setLongitude(-74.0060);
        updatedHospital.setAddress("456 Updated St");
        updatedHospital.setSpeciality("Emergency");
        updatedHospital.setAvailable(false);
        updatedHospital.setAmbulanceIds(Arrays.asList(3, 4));

        when(hospitalService.updateHospital(eq(1), any(Hospital.class)))
                .thenReturn(Optional.of(updatedHospital));

        mockMvc.perform(put("/hospitals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedHospital)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Hospital"))
                .andExpect(jsonPath("$.address").value("456 Updated St"))
                .andExpect(jsonPath("$.speciality").value("Emergency"))
                .andExpect(jsonPath("$.ambulanceIds").isArray());
    }

    @Test
    void updateHospital_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(hospitalService.updateHospital(eq(99), any(Hospital.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/hospitals/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Hospital())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHospital_WhenExists_ShouldReturnNoContent() throws Exception {
        when(hospitalService.deleteHospital(1)).thenReturn(true);

        mockMvc.perform(delete("/hospitals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteHospital_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(hospitalService.deleteHospital(99)).thenReturn(false);

        mockMvc.perform(delete("/hospitals/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAmbulanceToHospital_WhenSuccess_ShouldReturnOk() throws Exception {
        when(hospitalService.addAmbulanceToHospital(1, 1))
                .thenReturn(Optional.of(hospital));

        mockMvc.perform(post("/hospitals/1/ambulances/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ambulanceIds").isArray());
    }

    @Test
    void addAmbulanceToHospital_WhenHospitalNotFound_ShouldReturnNotFound() throws Exception {
        when(hospitalService.addAmbulanceToHospital(99, 1))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/hospitals/99/ambulances/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeAmbulanceFromHospital_WhenSuccess_ShouldReturnOk() throws Exception {
        when(hospitalService.removeAmbulanceFromHospital(1, 1))
                .thenReturn(Optional.of(hospital));

        mockMvc.perform(delete("/hospitals/1/ambulances/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ambulanceIds").isArray());
    }

    @Test
    void removeAmbulanceFromHospital_WhenHospitalNotFound_ShouldReturnNotFound() throws Exception {
        when(hospitalService.removeAmbulanceFromHospital(99, 1))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/hospitals/99/ambulances/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSpecialities_ShouldReturnSetOfSpecialities() throws Exception {
        Set<String> specialities = new HashSet<>(Arrays.asList("General", "Emergency"));
        when(hospitalService.getAllSpecialities()).thenReturn(specialities);

        mockMvc.perform(get("/hospitals/specialities"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder("General", "Emergency")));
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
                .andExpect(jsonPath("$[0].name").value("Test Hospital"))
                .andExpect(jsonPath("$[0].speciality").value("General"))
                .andExpect(jsonPath("$[0].ambulanceIds").isArray());
    }

    @Test
    void getAmbulancesByHospital_WhenHospitalExists_ShouldReturnAmbulances() throws Exception {
        Map<Integer, Object> ambulanceDetails = new HashMap<>();
        Map<String, Object> ambulance1 = new HashMap<>();
        ambulance1.put("id", 1);
        ambulance1.put("driverName", "John Doe");
        ambulance1.put("available", true);
        
        Map<String, Object> ambulance2 = new HashMap<>();
        ambulance2.put("id", 2);
        ambulance2.put("driverName", "Jane Smith");
        ambulance2.put("available", false);

        ambulanceDetails.put(1, ambulance1);
        ambulanceDetails.put(2, ambulance2);

        when(hospitalService.findByAmbulanceIds(1)).thenReturn(ambulanceDetails);

        mockMvc.perform(get("/hospitals/1/ambulances"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.1.id").value(1))
                .andExpect(jsonPath("$.1.driverName").value("John Doe"))
                .andExpect(jsonPath("$.1.available").value(true))
                .andExpect(jsonPath("$.2.id").value(2))
                .andExpect(jsonPath("$.2.driverName").value("Jane Smith"))
                .andExpect(jsonPath("$.2.available").value(false));
    }

    @Test
    void getAmbulancesByHospital_WhenNoAmbulances_ShouldReturnEmptyMap() throws Exception {
        when(hospitalService.findByAmbulanceIds(1)).thenReturn(new HashMap<>());

        mockMvc.perform(get("/hospitals/1/ambulances"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
    }
}
