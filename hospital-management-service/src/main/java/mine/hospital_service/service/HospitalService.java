package mine.hospital_service.service;

import mine.hospital_service.dto.AmbulanceDTO;
import mine.hospital_service.model.Hospital;
import mine.hospital_service.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final WebClient webClient;

    public HospitalService(WebClient.Builder webClientBuilder, HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
        this.webClient = webClientBuilder.baseUrl("http://ambulance-service").build();
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Optional<Hospital> getHospitalById(Integer id) {
        return hospitalRepository.findById(id);
    }

    public Hospital createHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    public Optional<Hospital> updateHospital(Integer id, Hospital updatedHospital) {
        return hospitalRepository.findById(id)
                .map(existingHospital -> {
                    existingHospital.setName(updatedHospital.getName());
                    existingHospital.setLatitude(updatedHospital.getLatitude());
                    existingHospital.setLongitude(updatedHospital.getLongitude());
                    existingHospital.setAvailable(updatedHospital.isAvailable());
                    existingHospital.setAmbulanceIds(updatedHospital.getAmbulanceIds());
                    existingHospital.setAddress(updatedHospital.getAddress());
                    existingHospital.setSpeciality(updatedHospital.getSpeciality());
                    return hospitalRepository.save(existingHospital);
                });
    }

    public boolean deleteHospital(Integer id) {
        return hospitalRepository.findById(id)
                .map(hospital -> {
                    hospitalRepository.delete(hospital);
                    return true;
                }).orElse(false);
    }

    public Optional<Hospital> addAmbulanceToHospital(Integer hospitalId, Integer ambulanceId) {
        return hospitalRepository.findById(hospitalId)
                .map(hospital -> {
                    if (!hospital.getAmbulanceIds().contains(ambulanceId)) {
                        hospital.getAmbulanceIds().add(ambulanceId);
                        return hospitalRepository.save(hospital);
                    }
                    return hospital;
                });
    }

    public Optional<Hospital> removeAmbulanceFromHospital(Integer hospitalId, Integer ambulanceId) {
        return hospitalRepository.findById(hospitalId)
                .map(hospital -> {
                    hospital.getAmbulanceIds().remove(ambulanceId);
                    return hospitalRepository.save(hospital);
                });
    }

    public Optional<AmbulanceDTO> fetchAmbulanceDetails(Integer ambulanceId) {
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri("/ambulances/{id}", ambulanceId)
                            .retrieve()
                            .bodyToMono(AmbulanceDTO.class)
                            .block()
            );
        } catch (Exception e) {
            System.err.println("Error fetching ambulance details: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Hospital> findBySpeciality(String speciality) {
        if (speciality == null || speciality.trim().isEmpty()) {
            throw new IllegalArgumentException("Speciality must not be null or empty.");
        }
        return hospitalRepository.findBySpeciality(speciality.trim());
    }

    public Map<Integer, Object> findByAmbulanceIds(Integer hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .map(hospital -> {
                    Map<Integer, Object> ambulanceDetails = new HashMap<>();
                    hospital.getAmbulanceIds().forEach(ambulanceId -> {
                        fetchAmbulanceDetails(ambulanceId).ifPresent(details -> {
                            ambulanceDetails.put(ambulanceId, details);
                        });
                    });
                    return ambulanceDetails;
                }).orElse(new HashMap<>());
    }

    public Set<String> getAllSpecialities() {
        return hospitalRepository.findAll().stream()
                .map(Hospital::getSpeciality)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
