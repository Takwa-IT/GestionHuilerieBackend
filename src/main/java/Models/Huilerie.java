package Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "huilerie", uniqueConstraints = {
        @UniqueConstraint(name = "uk_huilerie_nom", columnNames = "nom")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Huilerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHuilerie;

    @Column(nullable = false, unique = true)
    private String nom;
    private String localisation;
    private String type; // "artisanal", "semi-industriel", "industriel"
    private String certification;
    private Integer capaciteProduction;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    private Boolean active = true;

    @OneToMany(mappedBy = "huilerie", cascade = CascadeType.ALL)
    private List<Machine> machines;

    @OneToMany(mappedBy = "huilerie")
    private List<LotOlives> lots;

    @OneToMany(mappedBy = "huilerie", cascade = CascadeType.ALL)
    private List<CampagneOlives> campagnesOlives;

    public Long getIdHuilerie() {
        return idHuilerie;
    }

    public void setIdHuilerie(Long idHuilerie) {
        this.idHuilerie = idHuilerie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public Integer getCapaciteProduction() {
        return capaciteProduction;
    }

    public void setCapaciteProduction(Integer capaciteProduction) {
        this.capaciteProduction = capaciteProduction;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }

    public List<LotOlives> getLots() {
        return lots;
    }

    public void setLots(List<LotOlives> lots) {
        this.lots = lots;
    }

    public List<CampagneOlives> getCampagnesOlives() {
        return campagnesOlives;
    }

    public void setCampagnesOlives(List<CampagneOlives> campagnesOlives) {
        this.campagnesOlives = campagnesOlives;
    }

}
