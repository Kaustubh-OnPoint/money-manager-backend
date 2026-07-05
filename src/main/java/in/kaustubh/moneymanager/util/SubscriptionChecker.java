package in.kaustubh.moneymanager.util;

import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SubscriptionChecker {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public boolean isPremiumOrTrial(ProfileEntity profile) {
        if (profile.getSubscriptionTier() == SubscriptionTier.PREMIUM) {
            return true;
        }
        if (profile.getSubscriptionTier() == SubscriptionTier.TRIAL) {
            return profile.getTrialEndDate() != null && profile.getTrialEndDate().isAfter(java.time.LocalDateTime.now());
        }
        return false;
    }

    public void checkCanAddTransaction(ProfileEntity profile) {
        if (isPremiumOrTrial(profile)) {
            return;
        }
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        long incomeCount = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startOfMonth, endOfMonth).size();
        long expenseCount = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startOfMonth, endOfMonth).size();
        if (incomeCount + expenseCount >= 50) {
            throw new AccessDeniedException("Monthly transaction limit reached. Upgrade to Premium.");
        }
    }

    public void checkCanAddCategory(ProfileEntity profile) {
        if (isPremiumOrTrial(profile)) {
            return;
        }
        long categoryCount = categoryRepository.findByProfileId(profile.getId()).size();
        if (categoryCount >= 10) {
            throw new AccessDeniedException("Category limit reached. Upgrade to Premium.");
        }
    }

    public void checkCanUsePremiumFeature(ProfileEntity profile) {
        if (!isPremiumOrTrial(profile)) {
            throw new AccessDeniedException("Premium feature. Please upgrade.");
        }
    }
}
