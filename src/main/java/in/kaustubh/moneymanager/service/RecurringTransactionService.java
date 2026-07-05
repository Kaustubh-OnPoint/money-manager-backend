package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.RecurringTransactionDTO;
import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.entity.RecurringTransactionEntity;
import in.kaustubh.moneymanager.enums.TransactionType;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.RecurringTransactionRepository;
import in.kaustubh.moneymanager.util.SubscriptionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final SubscriptionChecker subscriptionChecker;

    public RecurringTransactionDTO createRecurring(RecurringTransactionDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        subscriptionChecker.checkCanUsePremiumFeature(profile);

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringTransactionEntity entity = RecurringTransactionEntity.builder()
                .profile(profile)
                .category(category)
                .name(dto.getName())
                .amount(dto.getAmount())
                .icon(dto.getIcon())
                .type(dto.getType() != null ? dto.getType() : TransactionType.EXPENSE)
                .nextExecutionDate(dto.getNextExecutionDate() != null ? dto.getNextExecutionDate() : LocalDate.now().plusMonths(1))
                .isActive(true)
                .build();

        entity = recurringTransactionRepository.save(entity);
        return toDTO(entity);
    }

    public List<RecurringTransactionDTO> getRecurringForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        subscriptionChecker.checkCanUsePremiumFeature(profile);
        return recurringTransactionRepository.findByProfileIdAndIsActiveTrue(profile.getId())
                .stream().map(this::toDTO).toList();
    }

    public void cancelRecurring(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        subscriptionChecker.checkCanUsePremiumFeature(profile);
        RecurringTransactionEntity entity = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        entity.setIsActive(false);
        recurringTransactionRepository.save(entity);
    }

    public RecurringTransactionDTO toDTO(RecurringTransactionEntity entity) {
        return RecurringTransactionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amount(entity.getAmount())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory().getId())
                .categoryName(entity.getCategory().getName())
                .type(entity.getType())
                .nextExecutionDate(entity.getNextExecutionDate())
                .isActive(entity.getIsActive())
                .build();
    }
}
