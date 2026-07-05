package in.kaustubh.moneymanager.dto;

import in.kaustubh.moneymanager.enums.Role;
import in.kaustubh.moneymanager.enums.SubscriptionStatus;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private String password;
    private String profileImageUrl;
    private Role role;
    private SubscriptionTier subscriptionTier;
    private LocalDateTime trialEndDate;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
