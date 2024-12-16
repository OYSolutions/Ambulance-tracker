package mine.hospital_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "hospitals")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private boolean available;

    @ElementCollection
    @CollectionTable(name = "hospital_ambulances", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "ambulance_id")
    private List<Integer> ambulanceIds; // Store ambulance IDs

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<Integer> getAmbulanceIds() {
        return ambulanceIds;
    }

    public void setAmbulanceIds(List<Integer> ambulanceIds) {
        this.ambulanceIds = ambulanceIds;
    }
}
