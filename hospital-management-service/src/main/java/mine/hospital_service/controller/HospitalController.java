package mine.hospital_service.controller;

import mine.hospital_service.dto.AmbulanceDTO;
import mine.hospital_service.model.Hospital;
import mine.hospital_service.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public List<Hospital> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable Integer id) {
        return hospitalService.getHospitalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
        if (hospital.getName() == null || hospital.getLatitude() == null || hospital.getLongitude() == null) {
            return ResponseEntity.badRequest().build();
        }
        Hospital createdHospital = hospitalService.createHospital(hospital);
        return ResponseEntity.ok(createdHospital);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hospital> updateHospital(@PathVariable Integer id, @RequestBody Hospital updatedHospital) {
        return hospitalService.updateHospital(id, updatedHospital)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Integer id) {
        boolean deleted = hospitalService.deleteHospital(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/ambulances/{ambulanceId}")
    public ResponseEntity<Hospital> addAmbulanceToHospital(@PathVariable Integer id, @PathVariable Integer ambulanceId) {
        return hospitalService.addAmbulanceToHospital(id, ambulanceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/ambulances/{ambulanceId}")
    public ResponseEntity<Hospital> removeAmbulanceFromHospital(@PathVariable Integer id, @PathVariable Integer ambulanceId) {
        return hospitalService.removeAmbulanceFromHospital(id, ambulanceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/ambulances/{ambulanceId}", produces = "application/json")
    public ResponseEntity<AmbulanceDTO> getAmbulanceDetails(@PathVariable Integer ambulanceId) {
        return hospitalService.fetchAmbulanceDetails(ambulanceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
