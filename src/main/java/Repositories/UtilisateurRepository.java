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

	@Query("""
		select u
		from Utilisateur u
		where (type(u) = Administrateur and treat(u as Administrateur).entrepriseAdmin is not null
		       and treat(u as Administrateur).entrepriseAdmin.idEntreprise = :entrepriseId)
		   or (type(u) = Employe and treat(u as Employe).huilerieEmp is not null
		       and treat(u as Employe).huilerieEmp.entreprise is not null
		       and treat(u as Employe).huilerieEmp.entreprise.idEntreprise = :entrepriseId)
		order by u.idUtilisateur asc
		""")
	List<Utilisateur> findAllByEntreprise_IdEntrepriseOrderByIdUtilisateurAsc(@Param("entrepriseId") Long entrepriseId);

	@Query("""
		select distinct u.profil
		from Utilisateur u
		where ((type(u) = Administrateur and treat(u as Administrateur).entrepriseAdmin is not null
		        and treat(u as Administrateur).entrepriseAdmin.idEntreprise = :entrepriseId)
		    or (type(u) = Employe and treat(u as Employe).huilerieEmp is not null
		        and treat(u as Employe).huilerieEmp.entreprise is not null
		        and treat(u as Employe).huilerieEmp.entreprise.idEntreprise = :entrepriseId))
		  and u.profil is not null
		order by u.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByEntrepriseIdOrderByIdProfilAsc(@Param("entrepriseId") Long entrepriseId);

	@Query("""
		select e
		from Employe e
		where e.huilerieEmp is not null
		  and e.huilerieEmp.idHuilerie in :huilerieIds
		order by e.idUtilisateur asc
		""")
	List<Utilisateur> findAllByHuilerieIdsOrderByIdUtilisateurAsc(@Param("huilerieIds") List<Long> huilerieIds);

	@Query("""
		select distinct e.profil
		from Employe e
		where e.huilerieEmp is not null
		  and e.huilerieEmp.idHuilerie in :huilerieIds
		  and e.profil is not null
		order by e.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByHuilerieIds(@Param("huilerieIds") List<Long> huilerieIds);

	@Query("""
		select distinct e.profil
		from Employe e
		where e.huilerieEmp is not null
		  and e.huilerieEmp.entreprise is not null
		  and e.huilerieEmp.entreprise.idEntreprise = :entrepriseId
		  and lower(e.huilerieEmp.nom) like lower(concat('%', :huilerieNom, '%'))
		  and e.profil is not null
		order by e.profil.idProfil asc
		""")
	List<Profil> findDistinctProfilsByEntrepriseIdAndHuilerieNom(@Param("entrepriseId") Long entrepriseId, @Param("huilerieNom") String huilerieNom);
}


