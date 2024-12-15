package mine.routeoptimizationservice.controller;

import mine.routeoptimizationservice.dto.RouteResponse;
import mine.routeoptimizationservice.service.RouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/routes")
    public RouteResponse getRoute(
            @RequestParam Double originLat,
            @RequestParam Double originLng,
            @RequestParam Double destLat,
            @RequestParam Double destLng) {
        return routeService.getOptimizedRoute(originLat, originLng, destLat, destLng);
    }
}
