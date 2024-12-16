package mine.routeoptimizationservice.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDirectionsResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("routes")
    private List<Route> routes;

    @JsonProperty("error_message")
    private String errorMessage; // Add this field for error messages

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public String getErrorMessage() { // Add getter
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) { // Add setter
        this.errorMessage = errorMessage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {

        @JsonProperty("overview_polyline")
        private OverviewPolyline overviewPolyline;

        // Add other fields as needed
        // Getters and Setters
        public OverviewPolyline getOverviewPolyline() {
            return overviewPolyline;
        }

        public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
            this.overviewPolyline = overviewPolyline;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverviewPolyline {

        @JsonProperty("points")
        private String points;

        // Getters and Setters
        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }
}
