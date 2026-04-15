package Repositories;

import Models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
	@Query("select p from Permission p join fetch p.module where p.profil.idProfil = :profilId")
	List<Permission> findByProfilIdWithModule(@Param("profilId") Long profilId);

	Optional<Permission> findByProfilIdProfilAndModuleIdModule(Long profilId, Long moduleId);
}


