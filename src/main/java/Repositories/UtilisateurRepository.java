package Repositories;

import Models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
	Optional<Utilisateur> findByEmail(String email);
	boolean existsByProfilIdProfil(Long idProfil);
	List<Utilisateur> findByProfilIdProfil(Long idProfil);
	List<Utilisateur> findAllByOrderByIdUtilisateurAsc();
}