package Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrateur")
public class Administrateur extends Utilisateur {

    @OneToOne
    @JoinColumn(name = "entreprise_id_admin", unique = true, nullable = false)
    private Entreprise entrepriseAdmin;

    public Long getIdAdministrateur() {
        return getIdUtilisateur();
    }

    public void setIdAdministrateur(Long idAdministrateur) {
        setIdUtilisateur(idAdministrateur);
    }

    public Entreprise getEntrepriseAdmin() {
        return entrepriseAdmin;
    }

    public void setEntrepriseAdmin(Entreprise entrepriseAdmin) {
        this.entrepriseAdmin = entrepriseAdmin;
    }
}
