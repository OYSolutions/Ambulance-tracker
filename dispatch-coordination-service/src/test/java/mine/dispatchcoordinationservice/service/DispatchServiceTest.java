package mine.dispatchcoordinationservice.service;

import mine.dispatchcoordinationservice.dto.*;
import mine.dispatchcoordinationservice.model.Case;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DispatchServiceTest {

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

    @Mock
    private CaseService caseService;

    @InjectMocks
    private DispatchService dispatchService;

    private Hospital hospital;
    private Ambulance ambulance;
    private EmergencyRequest emergencyRequest;
    private RouteResponse routeResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup WebClient mock chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Setup test data
        hospital = new Hospital();
        hospital.setId(1L);
        hospital.setName("Test Hospital");
        hospital.setSpecialization("Cardiology");

        ambulance = new Ambulance();
        ambulance.setId(1);
        ambulance.setAvailable(true);
        ambulance.setLatitude(40.7128);
        ambulance.setLongitude(-74.0060);

        hospital.setAmbulances(Collections.singletonList(ambulance));

        emergencyRequest = new EmergencyRequest();
        emergencyRequest.setLatitude(40.7589);
        emergencyRequest.setLongitude(-73.9851);
        emergencyRequest.setSpecialization("Cardiology");

        routeResponse = new RouteResponse();
        routeResponse.setGeometry("test_geometry");
        routeResponse.setDistance(1000.0);
        routeResponse.setDuration(600.0);
        routeResponse.setStatus("SUCCESS");
    }

    @Test
    void handleEmergency_Success() {
        // Mock hospital service response
        List<Hospital> hospitals = Collections.singletonList(hospital);
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(hospitals))
                .thenReturn(Mono.just(routeResponse));

        // Mock case service
        Case savedCase = new Case();
        savedCase.setId(1);
        when(caseService.createCase(any())).thenReturn(savedCase);

        // Execute test
        DispatchResult result = dispatchService.handleEmergency(emergencyRequest);

        // Verify
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(ambulance, result.getAssignedAmbulance());
        assertEquals(hospital, result.getAssignedHospital());
        assertEquals("test_geometry", result.getRoutePolyline());
    }

    @Test
    void handleEmergency_NoHospitalsFound() {
        // Mock empty hospital response
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(Collections.emptyList()));

        // Execute test
        DispatchResult result = dispatchService.handleEmergency(emergencyRequest);

        // Verify
        assertNotNull(result);
        assertEquals("FAILURE", result.getStatus());
        assertNull(result.getAssignedAmbulance());
        assertNull(result.getAssignedHospital());
    }

    @Test
    void handleEmergency_NoAvailableAmbulances() {
        // Setup hospital with no available ambulances
        ambulance.setAvailable(false);
        hospital.setAmbulances(Collections.singletonList(ambulance));
        List<Hospital> hospitals = Collections.singletonList(hospital);

        // Mock responses
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(hospitals));

        // Execute test
        DispatchResult result = dispatchService.handleEmergency(emergencyRequest);

        // Verify
        assertNotNull(result);
        assertEquals("FAILURE", result.getStatus());
        assertNull(result.getAssignedAmbulance());
        assertNull(result.getAssignedHospital());
    }

    @Test
    void handleEmergency_RouteOptimizationFailure() {
        // Mock successful hospital response but failed route response
        List<Hospital> hospitals = Collections.singletonList(hospital);
        RouteResponse failedRoute = new RouteResponse();
        failedRoute.setStatus("FAILURE");

        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(hospitals))
                .thenReturn(Mono.just(failedRoute));

        // Execute test
        DispatchResult result = dispatchService.handleEmergency(emergencyRequest);

        // Verify
        assertNotNull(result);
        assertEquals("FAILURE", result.getStatus());
        assertNull(result.getAssignedAmbulance());
        assertNull(result.getAssignedHospital());
    }

    @Test
    void handleEmergency_MultipleHospitalsAndAmbulances() {
        // Setup multiple hospitals and ambulances
        Hospital hospital2 = new Hospital();
        hospital2.setId(2L);
        hospital2.setSpecialization("Cardiology");

        Ambulance ambulance2 = new Ambulance();
        ambulance2.setId(2);
        ambulance2.setAvailable(true);
        ambulance2.setLatitude(40.7);
        ambulance2.setLongitude(-74.0);

        hospital2.setAmbulances(Collections.singletonList(ambulance2));

        List<Hospital> hospitals = Arrays.asList(hospital, hospital2);

        // Mock responses
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.just(hospitals))
                .thenReturn(Mono.just(routeResponse));

        // Mock case service
        Case savedCase = new Case();
        savedCase.setId(1);
        when(caseService.createCase(any())).thenReturn(savedCase);

        // Execute test
        DispatchResult result = dispatchService.handleEmergency(emergencyRequest);

        // Verify
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getAssignedAmbulance());
        assertNotNull(result.getAssignedHospital());
        assertEquals("test_geometry", result.getRoutePolyline());
    }
}
