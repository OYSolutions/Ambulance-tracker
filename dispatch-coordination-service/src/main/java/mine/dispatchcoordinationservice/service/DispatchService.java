package mine.dispatchcoordinationservice.service;

import mine.dispatchcoordinationservice.dto.*;
import mine.dispatchcoordinationservice.model.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private static final String HOSPITAL_MANAGEMENT_SERVICE_URL = "http://hospital-management-service";
    private static final String ROUTE_OPTIMIZATION_SERVICE_URL = "http://route-optimization-service";
    private static final String AMBULANCE_SERVICE_URL = "http://ambulance-service";

    private final WebClient webClient;

    private final CaseService caseService;

    public DispatchService(WebClient.Builder webClientBuilder, CaseService caseService) {
        this.webClient = webClientBuilder.build();
        this.caseService = caseService;
    }

    public DispatchResult handleEmergency(EmergencyRequest request) {
        // Step 1: Get hospitals matching specialization
        List<Hospital> hospitals = fetchHospitalsBySpeciality(request.getSpecialization());

        if (hospitals.isEmpty()) {
            return createFailureResult("No hospital with the required specialization found.");
        }

        // Step 2: Retrieve ambulances assigned to these hospitals
        List<AmbulanceHospitalPair> ambulanceHospitalPairs = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            List<Ambulance> hospitalAmbulances = fetchAmbulancesByHospital(hospital.getId());
            for (Ambulance ambulance : hospitalAmbulances) {
                if (ambulance.isAvailable()) {
                    ambulanceHospitalPairs.add(new AmbulanceHospitalPair(ambulance, hospital));
                }
            }
        }

        if (ambulanceHospitalPairs.isEmpty()) {
            return createFailureResult("No available ambulances found for the required specialization.");
        }

        // Step 3: Find the nearest available ambulance
        Optional<AmbulanceHospitalPair> nearestAmbulance = ambulanceHospitalPairs.stream()
                .min(Comparator.comparingDouble(pair -> calculateDistance(
                        pair.getAmbulance().getLatitude(),
                        pair.getAmbulance().getLongitude(),
                        request.getLatitude(),
                        request.getLongitude()
                )));

        if (nearestAmbulance.isEmpty()) {
            return createFailureResult("No suitable ambulance found.");
        }

        AmbulanceHospitalPair selectedPair = nearestAmbulance.get();

        // Step 4: Calculate the route
        RouteResponse routeResponse = fetchRoute(selectedPair.getAmbulance(), request);

        if (!"SUCCESS".equals(routeResponse.getStatus())) {
            return createFailureResult("Route calculation failed.");
        }

        // Step 5: Mark the ambulance as unavailable
        boolean updateSuccess = updateAmbulanceAvailability(selectedPair.getAmbulance().getId(), false);

        if (!updateSuccess) {
            return createFailureResult("Failed to update ambulance availability.");
        }

        // Step 6: Save the dispatch case in the database
        Case newCase = new Case();
        newCase.setLatitude(request.getLatitude());
        newCase.setLongitude(request.getLongitude());
        newCase.setSpecialization(request.getSpecialization());
        newCase.setStatus("OPEN");
        newCase.setAssigned_ambulance_id(selectedPair.getAmbulance().getId());
        newCase.setAssigned_hospital_id(selectedPair.getHospital().getId());
        newCase.setEstimatedDuration(routeResponse.getDuration());
        newCase.setCreatedAt(LocalDateTime.now());

        log.debug("Saving case with ambulance ID: {}", newCase.getAssigned_ambulance_id());
        log.debug("Saving case with hospital ID: {}", newCase.getAssigned_hospital_id());

        caseService.createCase(newCase);

        // Step 7: Return DispatchResult
        DispatchResult dispatchResult = new DispatchResult();
        dispatchResult.setAssignedAmbulance(selectedPair.getAmbulance());
        dispatchResult.setAssignedHospital(selectedPair.getHospital());
        dispatchResult.setRoutePolyline(routeResponse.getGeometry());
        dispatchResult.setStatus("SUCCESS");
        return dispatchResult;
    }




    public List<Hospital> fetchHospitalsBySpeciality(String speciality) {
        try {
            return webClient.get()
                    .uri("http://localhost:8888/hospital-management-service/hospitals?speciality=" + speciality)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Error fetching hospitals")))
                    .bodyToFlux(Hospital.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching hospitals by speciality: {}", speciality, e);
            return new ArrayList<>();
        }
    }

    public List<Ambulance> fetchAmbulancesByHospital(Long hospitalId) {
        try {
            return webClient.get()
                    .uri("http://localhost:8888/hospital-management-service/hospitals/by-hospital/" + hospitalId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Error fetching ambulances")))
                    .bodyToFlux(Ambulance.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching ambulances for hospital ID: {}", hospitalId, e);
            return new ArrayList<>();
        }
    }

    private RouteResponse fetchRoute(Ambulance ambulance, EmergencyRequest request) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost") // Replace with actual host
                            .port(8888) // Replace with actual port
                            .path("/route-optimization-service/routes")
                            .queryParam("originLat", ambulance.getLatitude())
                            .queryParam("originLng", ambulance.getLongitude())
                            .queryParam("destLat", request.getLatitude())
                            .queryParam("destLng", request.getLongitude())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Error calculating route")))
                    .bodyToMono(RouteResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException: {}", e.getMessage());
            return new RouteResponse("FAILURE", null);
        } catch (Exception e) {
            log.error("Error fetching route for ambulance ID: {}", ambulance.getId(), e);
            return new RouteResponse("FAILURE", null);
        }
    }

    private boolean updateAmbulanceAvailability(Integer ambulanceId, boolean availability) {
        try {
            String url = "http://localhost:8888/ambulance-service/ambulances/" + ambulanceId + "/availability";
            webClient.put()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("available", availability))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new RuntimeException("Error updating ambulance availability"))
                    )
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error updating availability for ambulance ID: {}", ambulanceId, e);
            return false;
        }
    }


    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double R = 6371e3; // Earth radius in meters
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private DispatchResult createFailureResult(String message) {
        DispatchResult result = new DispatchResult();
        result.setStatus("FAILURE");
        result.setRoutePolyline(null);
        return result;
    }

    // Helper class to pair Ambulances with Hospitals
    private static class AmbulanceHospitalPair {
        private final Ambulance ambulance;
        private final Hospital hospital;

        public AmbulanceHospitalPair(Ambulance ambulance, Hospital hospital) {
            this.ambulance = ambulance;
            this.hospital = hospital;
        }

        public Ambulance getAmbulance() {
            return ambulance;
        }

        public Hospital getHospital() {
            return hospital;
        }
    }
}
