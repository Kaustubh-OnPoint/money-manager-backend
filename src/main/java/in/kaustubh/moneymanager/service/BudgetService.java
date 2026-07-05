package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.BudgetDTO;
import in.kaustubh.moneymanager.entity.BudgetEntity;
import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.BudgetRepository;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public BudgetDTO saveOrUpdateBudget(BudgetDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Optional<BudgetEntity> existing = budgetRepository.findByProfileIdAndCategoryIdAndMonthAndYear(
                profile.getId(), dto.getCategoryId(), dto.getMonth(), dto.getYear());

        BudgetEntity entity = existing.orElseGet(() -> BudgetEntity.builder()
                .profile(profile)
                .category(category)
                .month(dto.getMonth())
                .year(dto.getYear())
                .build());

        entity.setAmount(dto.getAmount());
        entity = budgetRepository.save(entity);
        return toDTO(entity);
    }

    public List<BudgetDTO> getBudgetsForMonth(Integer month, Integer year) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<BudgetEntity> budgets = budgetRepository.findByProfileIdAndMonthAndYear(profile.getId(), month, year);
        return budgets.stream().map(this::toDTO).toList();
    }

    public BudgetDTO toDTO(BudgetEntity entity) {
        BigDecimal spent = getSpentAmount(entity.getProfile().getId(), entity.getCategory().getId(), entity.getMonth(), entity.getYear());
        double percentage = 0.0;
        if (entity.getAmount() != null && entity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.multiply(BigDecimal.valueOf(100))
                    .divide(entity.getAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return BudgetDTO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory().getId())
                .categoryName(entity.getCategory().getName())
                .amount(entity.getAmount())
                .month(entity.getMonth())
                .year(entity.getYear())
                .spentAmount(spent)
                .percentageUsed(percentage)
                .build();
    }

    private BigDecimal getSpentAmount(Long profileId, Long categoryId, Integer month, Integer year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        BigDecimal spent = expenseRepository.sumAmountByProfileIdAndCategoryIdAndDateBetween(
                profileId, categoryId, startDate, endDate);
        return spent != null ? spent : BigDecimal.ZERO;
    }
}
