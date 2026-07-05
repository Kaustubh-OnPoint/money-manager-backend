package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.SubscriptionStatus;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import in.kaustubh.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrialExpirationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 2 * * *", zone = "IST")
    public void expireTrials() {
        log.info("Job started: expireTrials()");
        List<ProfileEntity> expiredProfiles = profileRepository
                .findBySubscriptionTierAndTrialEndDateBefore(SubscriptionTier.TRIAL, LocalDateTime.now());
        for (ProfileEntity profile : expiredProfiles) {
            profile.setSubscriptionTier(SubscriptionTier.FREE);
            profile.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            profileRepository.save(profile);
            log.info("Trial expired for user: {}", profile.getEmail());
        }
        log.info("Job completed: expireTrials() — {} trials expired", expiredProfiles.size());
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendTrialExpirationReminders() {
        log.info("Job started: sendTrialExpirationReminders()");
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        LocalDateTime threeDaysFromNowEnd = threeDaysFromNow.plusDays(1);
        List<ProfileEntity> profiles = profileRepository
                .findBySubscriptionTierAndTrialEndDateBetween(SubscriptionTier.TRIAL, threeDaysFromNow, threeDaysFromNowEnd);
        for (ProfileEntity profile : profiles) {
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "Your Money Manager trial expires in 3 days. Upgrade to Premium for just Rs 99 (one-time) to keep all features.<br><br>"
                    + "Best regards,<br>Money Manager Team";
            emailService.sendEmail(profile.getEmail(), "Trial expires in 3 days — Upgrade now", body);
            log.info("Sent trial reminder to: {}", profile.getEmail());
        }
        log.info("Job completed: sendTrialExpirationReminders()");
    }
}
