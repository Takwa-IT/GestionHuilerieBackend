package Repositories;

import Models.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProfilRepository extends JpaRepository<Profil, Long> {
	Optional<Profil> findByNom(String nom);

	List<Profil> findAllByOrderByIdProfilAsc();
}


