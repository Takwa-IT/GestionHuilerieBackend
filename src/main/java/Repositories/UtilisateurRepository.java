package Repositories;

import Models.Utilisateur;
import Models.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
	Optional<Utilisateur> findByEmail(String email);
	boolean existsByEmail(String email);
	Optional<Utilisateur> findByVerificationToken(String verificationToken);
	boolean existsByProfilIdProfil(Long idProfil);
	List<Utilisateur> findByProfilIdProfil(Long idProfil);
	List<Utilisateur> findAllByOrderByIdUtilisateurAsc();
	List<Utilisateur> findAllByEntreprise_IdEntrepriseOrderByIdUtilisateurAsc(Long entrepriseId);

	@Query("""
		select distinct u.profil
		from Utilisateur u
		where u.entreprise is not null
		  and u.entreprise.idEntreprise = :entrepriseId
		  and u.profil is not null
		order by u.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByEntrepriseIdOrderByIdProfilAsc(@Param("entrepriseId") Long entrepriseId);

	@Query("""
		select u
		from Utilisateur u
		where u.huilerie is not null
		  and u.huilerie.idHuilerie in :huilerieIds
		order by u.idUtilisateur asc
		""")
	List<Utilisateur> findAllByHuilerieIdsOrderByIdUtilisateurAsc(@Param("huilerieIds") List<Long> huilerieIds);

	@Query("""
		select distinct u.profil
		from Utilisateur u
		where u.huilerie is not null
		  and u.huilerie.idHuilerie in :huilerieIds
		  and u.profil is not null
		order by u.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByHuilerieIds(@Param("huilerieIds") List<Long> huilerieIds);

	@Query("""
		select distinct u.profil
		from Utilisateur u
		where u.entreprise is not null
		  and u.entreprise.idEntreprise = :entrepriseId
		  and u.huilerie is not null
		  and lower(u.huilerie.nom) like lower(concat('%', :huilerieNom, '%'))
		  and u.profil is not null
		order by u.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByEntrepriseIdAndHuilerieNom(@Param("entrepriseId") Long entrepriseId, @Param("huilerieNom") String huilerieNom);
}


