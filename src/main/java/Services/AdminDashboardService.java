package Services;

import Models.Administrateur;
import Models.Employe;
import Models.Huilerie;
import Models.StatutUtilisateur;
import Models.Utilisateur;
import Mapper.HuilerieMapper;
import Repositories.HuilerieRepository;
import Repositories.ProfilRepository;
import Repositories.UtilisateurRepository;
import dto.AdminDashboardDTO;
import dto.HuilerieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final CurrentUserService currentUserService;
    private final ProductionDashboardService productionDashboardService;
    private final HuilerieRepository huilerieRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HuilerieMapper huilerieMapper;

    public List<HuilerieDTO> listHuileries() {
        Long entrepriseId = requireAdminEntrepriseId();
        return huilerieRepository.findByEntreprise_IdEntreprise(entrepriseId).stream()
                .sorted(Comparator.comparing(Huilerie::getIdHuilerie))
                .map(huilerieMapper::toDTO)
                .toList();
    }

    public AdminDashboardDTO buildDashboard(LocalDate dateFrom, LocalDate dateTo, Long huilerieId) {
        requireAdminAccess();

        Long entrepriseId = currentUserService.getCurrentEntrepriseIdOrThrow();
        List<Huilerie> scopedHuileries = resolveScopedHuileries(entrepriseId, huilerieId);
        List<Utilisateur> scopedUsers = resolveScopedUsers(entrepriseId, huilerieId);

        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setAllHuileries(huilerieId == null);
        dto.setSelectedHuilerieId(huilerieId);
        dto.setSelectedHuilerieNom(resolveSelectedHuilerieName(entrepriseId, huilerieId));
        dto.setProductionDashboard(productionDashboardService.buildDashboard(dateFrom, dateTo, huilerieId));
        dto.setUserStats(buildUserStats(scopedUsers, scopedHuileries));
        return dto;
    }

    public List<HuilerieDTO> findHuileries() {
        return listHuileries();
    }

    private AdminDashboardDTO.UserStatsDTO buildUserStats(List<Utilisateur> users, List<Huilerie> scopedHuileries) {
        AdminDashboardDTO.UserStatsDTO stats = new AdminDashboardDTO.UserStatsDTO();

        long totalUsers = users.size();
        long activeUsers = users.stream()
                .filter(user -> StatutUtilisateur.ACTIF.equals(user.getActif()))
                .count();
        long inactiveUsers = totalUsers - activeUsers;
        long adminUsers = users.stream().filter(user -> user instanceof Administrateur).count();
        long employeeUsers = users.stream().filter(user -> user instanceof Employe).count();

        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setInactiveUsers(inactiveUsers);
        stats.setAdminUsers(adminUsers);
        stats.setEmployeeUsers(employeeUsers);
        stats.setProfilesConfiguredCount(profilRepository.count());
        stats.setProfiles(buildProfileStats(users));
        stats.setHuileries(buildHuilerieStats(users, scopedHuileries));
        stats.setIncludedUserIds(users.stream()
                .filter(Objects::nonNull)
                .map(Utilisateur::getIdUtilisateur)
                .toList());

        return stats;
    }

    private List<AdminDashboardDTO.ProfileStatDTO> buildProfileStats(List<Utilisateur> users) {
        Map<Long, List<Utilisateur>> grouped = users.stream()
                .filter(user -> user.getProfil() != null)
                .collect(Collectors.groupingBy(user -> user.getProfil().getIdProfil()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<Utilisateur> profileUsers = entry.getValue();
                    AdminDashboardDTO.ProfileStatDTO dto = new AdminDashboardDTO.ProfileStatDTO();
                    dto.setProfilId(entry.getKey());
                    dto.setProfilNom(profileUsers.stream()
                            .map(user -> user.getProfil() != null ? user.getProfil().getNom() : null)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null));
                    dto.setUsersCount((long) profileUsers.size());
                    dto.setActiveUsers(profileUsers.stream()
                            .filter(user -> StatutUtilisateur.ACTIF.equals(user.getActif()))
                            .count());
                    return dto;
                })
                .sorted(Comparator.comparing(AdminDashboardDTO.ProfileStatDTO::getProfilId))
                .toList();
    }

    private List<AdminDashboardDTO.HuilerieStatDTO> buildHuilerieStats(List<Utilisateur> users, List<Huilerie> scopedHuileries) {
        return scopedHuileries.stream()
                .map(huilerie -> {
                    List<Utilisateur> huilerieUsers = users.stream()
                            .filter(user -> matchesHuilerie(user, huilerie.getIdHuilerie()))
                            .toList();

                    AdminDashboardDTO.HuilerieStatDTO dto = new AdminDashboardDTO.HuilerieStatDTO();
                    dto.setHuilerieId(huilerie.getIdHuilerie());
                    dto.setHuilerieNom(huilerie.getNom());
                    dto.setUsersCount((long) huilerieUsers.size());
                    dto.setActiveUsers(huilerieUsers.stream()
                            .filter(user -> StatutUtilisateur.ACTIF.equals(user.getActif()))
                            .count());
                    return dto;
                })
                .sorted(Comparator.comparing(AdminDashboardDTO.HuilerieStatDTO::getHuilerieId))
                .toList();
    }

    private List<Utilisateur> resolveScopedUsers(Long entrepriseId, Long huilerieId) {
        if (huilerieId == null) {
            return utilisateurRepository.findAllByEntreprise_IdEntrepriseOrderByIdUtilisateurAsc(entrepriseId);
        }

        currentUserService.ensureCanAccessHuilerie(huilerieId);

        // Fetch employees for the selected huilerie(s)
        List<Utilisateur> employees = utilisateurRepository.findAllByHuilerieIdsOrderByIdUtilisateurAsc(List.of(huilerieId));

        // Fetch enterprise-level administrators to always include them
        List<Utilisateur> admins = utilisateurRepository.findAdministrateursByEntrepriseId(entrepriseId);

        // Merge admins and employees without duplicates (by id)
        Map<Long, Utilisateur> byId = employees.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(u -> u.getIdUtilisateur(), u -> u));

        for (Utilisateur admin : admins) {
            if (admin != null && !byId.containsKey(admin.getIdUtilisateur())) {
                byId.put(admin.getIdUtilisateur(), admin);
            }
        }

        return byId.values().stream()
                .sorted(Comparator.comparing(Utilisateur::getIdUtilisateur))
                .toList();
    }

    private List<Huilerie> resolveScopedHuileries(Long entrepriseId, Long huilerieId) {
        if (huilerieId == null) {
            return huilerieRepository.findByEntreprise_IdEntreprise(entrepriseId).stream()
                    .sorted(Comparator.comparing(Huilerie::getIdHuilerie))
                    .toList();
        }

        currentUserService.ensureCanAccessHuilerie(huilerieId);
        Huilerie huilerie = huilerieRepository.findById(huilerieId)
                .orElseThrow(() -> new IllegalArgumentException("Huilerie introuvable"));
        return List.of(huilerie);
    }

    private String resolveSelectedHuilerieName(Long entrepriseId, Long huilerieId) {
        if (huilerieId == null) {
            return "Toutes les huileries";
        }

        return huilerieRepository.findById(huilerieId)
                .filter(huilerie -> huilerie.getEntreprise() != null
                        && entrepriseId.equals(huilerie.getEntreprise().getIdEntreprise()))
                .map(Huilerie::getNom)
                .orElseThrow(() -> new AccessDeniedException("Acces refuse a une huilerie d'une autre entreprise"));
    }

    private boolean matchesHuilerie(Utilisateur user, Long huilerieId) {
        if (user instanceof Employe employe) {
            return employe.getHuilerieEmp() != null && huilerieId.equals(employe.getHuilerieEmp().getIdHuilerie());
        }

        Huilerie huilerie = user.getHuilerie();
        return huilerie != null && huilerie.getIdHuilerie() != null && huilerieId.equals(huilerie.getIdHuilerie());
    }

    private Long requireAdminEntrepriseId() {
        requireAdminAccess();
        return currentUserService.getCurrentEntrepriseIdOrThrow();
    }

    private void requireAdminAccess() {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (!currentUserService.isAdmin(utilisateur)) {
            throw new AccessDeniedException("Acces reserve a l'administrateur");
        }
    }
}