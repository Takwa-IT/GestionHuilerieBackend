package Models;

import Config.ReferenceUtils;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "machine")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMachine;

    @Column(unique = true)
    private String reference;

    private String nomMachine;
    private String typeMachine;
    private String categorieMachine;
    private String etatMachine;
    private Integer capacite;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @OneToMany(mappedBy = "machine")
    private List<EtapeProduction> etapesProduction;

    public Long getIdMachine() {
        return idMachine;
    }

    public void setIdMachine(Long idMachine) {
        this.idMachine = idMachine;
    }

    public String getReference() {
        if (idMachine != null) {
            return ReferenceUtils.format("MC", idMachine);
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNomMachine() {
        return nomMachine;
    }

    public void setNomMachine(String nomMachine) {
        this.nomMachine = nomMachine;
    }

    public String getTypeMachine() {
        return typeMachine;
    }

    public void setTypeMachine(String typeMachine) {
        this.typeMachine = typeMachine;
    }

    public String getEtatMachine() {
        return etatMachine;
    }

    public String getCategorieMachine() {
        return categorieMachine;
    }

    public void setCategorieMachine(String categorieMachine) {
        this.categorieMachine = categorieMachine;
    }

    public void setEtatMachine(String etatMachine) {
        this.etatMachine = etatMachine;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Huilerie getHuilerie() {
        return huilerie;
    }

    public void setHuilerie(Huilerie huilerie) {
        this.huilerie = huilerie;
    }

    public List<EtapeProduction> getEtapesProduction() {
        return etapesProduction;
    }

    public void setEtapesProduction(List<EtapeProduction> etapesProduction) {
        this.etapesProduction = etapesProduction;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idMachine != null) {
            reference = ReferenceUtils.format("MC", idMachine);
        }
    }

}
