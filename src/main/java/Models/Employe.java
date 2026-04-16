package Models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employe")
@DiscriminatorValue("EMPLOYE")
public class Employe extends Utilisateur {

    @Column(name = "id_employe")
    private Long idEmploye;

    @ManyToOne(optional = true)
    @JoinColumn(name = "huilerie_id_emp", nullable = true)
    private Huilerie huilerieEmp;

    public Long getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(Long idEmploye) {
        this.idEmploye = idEmploye;
    }

    public Huilerie getHuilerieEmp() {
        return huilerieEmp;
    }

    public void setHuilerieEmp(Huilerie huilerieEmp) {
        this.huilerieEmp = huilerieEmp;
    }
}
