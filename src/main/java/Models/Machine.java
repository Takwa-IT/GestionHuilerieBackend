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
    private String etatMachine;
    private Integer capacite;

    @ManyToOne
    @JoinColumn(name = "huilerie_id")
    private Huilerie huilerie;

    @ManyToOne
    @JoinColumn(name = "matiere_premiere_id")
    private MatierePremiere matierePremiere;

    @OneToMany(mappedBy = "machine")
    private List<ExecutionProduction> executionsProduction;

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

    public MatierePremiere getMatierePremiere() {
        return matierePremiere;
    }

    public void setMatierePremiere(MatierePremiere matierePremiere) {
        this.matierePremiere = matierePremiere;
    }

    public List<ExecutionProduction> getExecutionsProduction() {
        return executionsProduction;
    }

    public void setExecutionsProduction(List<ExecutionProduction> executionsProduction) {
        this.executionsProduction = executionsProduction;
    }

    @PostPersist
    public void buildReferenceAfterPersist() {
        if (reference == null && idMachine != null) {
            reference = ReferenceUtils.format("MC", idMachine);
        }
    }

}
