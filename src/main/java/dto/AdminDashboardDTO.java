package dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardDTO {

    private Long selectedHuilerieId;
    private String selectedHuilerieNom;
    private Boolean allHuileries;
    private ProductionDashboardDTO productionDashboard;
    private UserStatsDTO userStats;

    @Data
    public static class UserStatsDTO {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
        private Long adminUsers;
        private Long employeeUsers;
        private Long profilesConfiguredCount;
        private List<ProfileStatDTO> profiles;
        private List<HuilerieStatDTO> huileries;
        // Debug: list of user ids included in the scoped calculation
        private List<Long> includedUserIds;
    }

    @Data
    public static class ProfileStatDTO {
        private Long profilId;
        private String profilNom;
        private Long usersCount;
        private Long activeUsers;
    }

    @Data
    public static class HuilerieStatDTO {
        private Long huilerieId;
        private String huilerieNom;
        private Long usersCount;
        private Long activeUsers;
    }
}