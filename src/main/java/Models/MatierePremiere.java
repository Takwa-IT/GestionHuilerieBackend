package Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class MatierePremiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMatierePremiere;

    private String nom;
    private String type;
    private String uniteMesure;
    private String description;

    @OneToMany(mappedBy = "matierePremiere")
    private List<LotOlives> lots;

    @OneToMany(mappedBy = "matierePremiere")
    private List<Machine> machinesAffectees;

    public Long getIdMatierePremiere() { return idMatierePremiere; }
    public void setIdMatierePremiere(Long idMatierePremiere) { this.idMatierePremiere = idMatierePremiere; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUniteMesure() { return uniteMesure; }
    public void setUniteMesure(String uniteMesure) { this.uniteMesure = uniteMesure; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<LotOlives> getLots() { return lots; }
    public void setLots(List<LotOlives> lots) { this.lots = lots; }

    public List<Machine> getMachinesAffectees() { return machinesAffectees; }
    public void setMachinesAffectees(List<Machine> machinesAffectees) { this.machinesAffectees = machinesAffectees; }
}
