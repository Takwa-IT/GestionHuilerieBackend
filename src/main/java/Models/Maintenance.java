package Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMaintenance;

    private String dateMaintenance;
    private String type;
    private String observations;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;

    public Long getIdMaintenance() { return idMaintenance; }
    public void setIdMaintenance(Long idMaintenance) { this.idMaintenance = idMaintenance; }

    public String getDateMaintenance() { return dateMaintenance; }
    public void setDateMaintenance(String dateMaintenance) { this.dateMaintenance = dateMaintenance; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }
}