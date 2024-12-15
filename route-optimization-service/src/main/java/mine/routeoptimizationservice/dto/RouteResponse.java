package mine.routeoptimizationservice.dto;

public class RouteResponse {

    private String status;
    private String polyline;

    // Constructor
    public RouteResponse(String status, String polyline) {
        this.status = status;
        this.polyline = polyline;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }
}
