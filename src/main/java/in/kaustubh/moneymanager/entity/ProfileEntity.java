package in.kaustubh.moneymanager.entity;

import in.kaustubh.moneymanager.enums.Role;
import in.kaustubh.moneymanager.enums.SubscriptionStatus;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    @Column(unique = true)
    private String email;
    private String password;
    private String profileImageUrl;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private String activationToken;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.TRIAL;

    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private String razorpayPaymentId;
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = false;
        }
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.subscriptionTier == null) {
            this.subscriptionTier = SubscriptionTier.TRIAL;
        }
        if (this.trialStartDate == null) {
            this.trialStartDate = LocalDateTime.now();
        }
        if (this.trialEndDate == null) {
            this.trialEndDate = LocalDateTime.now().plusDays(30);
        }
        if (this.subscriptionStatus == null) {
            this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        }
    }

}
