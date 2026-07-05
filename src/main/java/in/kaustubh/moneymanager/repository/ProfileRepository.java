package in.kaustubh.moneymanager.repository;

import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationToken(String activationToken);

    List<ProfileEntity> findBySubscriptionTierAndTrialEndDateBefore(SubscriptionTier tier, LocalDateTime date);

    List<ProfileEntity> findBySubscriptionTierAndTrialEndDateBetween(SubscriptionTier tier, LocalDateTime start, LocalDateTime end);

    long countBySubscriptionTier(SubscriptionTier tier);
}
