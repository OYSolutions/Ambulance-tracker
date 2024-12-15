package mine.routeoptimizationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mine.routeoptimizationservice.dto.GoogleDirectionsResponse;
import mine.routeoptimizationservice.dto.RouteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RouteService {

    private final WebClient webClient;
    @Value("${google.maps.api-key}")
    private String apiKey;

    public RouteService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://maps.googleapis.com").build();
    }

    public RouteResponse getOptimizedRoute(Double originLat, Double originLng, Double destLat, Double destLng) {
        String url = String.format(
                "/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s&mode=driving",
                originLat, originLng, destLat, destLng, apiKey
        );

        try {
            // Fetch raw response
            String rawResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Log raw response
            System.out.println("Google Maps API Raw Response: " + rawResponse);

            // Parse JSON response to DTO
            ObjectMapper objectMapper = new ObjectMapper();
            GoogleDirectionsResponse response = objectMapper.readValue(rawResponse, GoogleDirectionsResponse.class);

            // Check for errors
            if (!"OK".equals(response.getStatus())) {
                String errorMessage = response.getErrorMessage();
                System.err.println("Google Maps API Error: " + errorMessage);
                return new RouteResponse("FAILURE", null);
            }

            // Check for missing routes
            if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
                System.err.println("No routes found in Google Maps API response.");
                return new RouteResponse("FAILURE", null);
            }

            // Extract polyline
            GoogleDirectionsResponse.Route route = response.getRoutes().get(0);
            if (route.getOverviewPolyline() == null || route.getOverviewPolyline().getPoints() == null) {
                System.err.println("No overviewPolyline found in the route.");
                return new RouteResponse("FAILURE", null);
            }

            return new RouteResponse("SUCCESS", route.getOverviewPolyline().getPoints());

        } catch (Exception e) {
            e.printStackTrace();
            return new RouteResponse("FAILURE", null);
        }
    }
}
