package in.kaustubh.moneymanager.dto;

import in.kaustubh.moneymanager.enums.Role;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {

    private Long id;
    private String fullName;
    private String email;
    private SubscriptionTier subscriptionTier;
    private LocalDateTime trialEndDate;
    private LocalDateTime createdAt;
    private Long totalTransactions;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private Role role;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
}
