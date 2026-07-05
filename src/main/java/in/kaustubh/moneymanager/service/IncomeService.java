package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.IncomeDTO;
import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.IncomeEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import in.kaustubh.moneymanager.util.SubscriptionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final SubscriptionChecker subscriptionChecker;

    private static final int TRANSACTION_NAME_MAX_LENGTH = 100;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        subscriptionChecker.checkCanAddTransaction(profile);

        String sanitizedName = validateAndSanitizeName(dto.getName());
        BigDecimal amount = validateAmount(dto.getAmount());
        LocalDate date = validateDate(dto.getDate());
        CategoryEntity category = validateCategoryOwnership(dto.getCategoryId(), profile);

        IncomeEntity newIncome = IncomeEntity.builder()
                .name(sanitizedName)
                .icon(dto.getIcon())
                .notes(dto.getNotes())
                .amount(amount)
                .date(date)
                .profile(profile)
                .category(category)
                .build();
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);
    }

    // Retrieves ALL incomes for current user, sorted by date descending
    public List<IncomeDTO> getAllIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Retrieves all incomes for current month (used by dashboard / stats)
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this income");
        }
        incomeRepository.delete(entity);
    }

    public IncomeDTO updateIncome(Long incomeId, IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this income");
        }

        String sanitizedName = validateAndSanitizeName(dto.getName());
        BigDecimal amount = validateAmount(dto.getAmount());
        LocalDate date = validateDate(dto.getDate());
        CategoryEntity category = validateCategoryOwnership(dto.getCategoryId(), profile);

        entity.setName(sanitizedName);
        entity.setAmount(amount);
        entity.setDate(date);
        entity.setIcon(dto.getIcon());
        entity.setNotes(dto.getNotes());
        entity.setCategory(category);
        entity = incomeRepository.save(entity);
        return toDTO(entity);
    }

    // Get latest 5 incomes for current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get total incomes for current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total: BigDecimal.ZERO;
    }

    //filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // Validation helpers
    private String validateAndSanitizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        String trimmed = rawName.trim();
        if (trimmed.length() > TRANSACTION_NAME_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be at most " + TRANSACTION_NAME_MAX_LENGTH + " characters");
        }
        if (!trimmed.matches(".*[\\p{L}\\d].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must contain at least one letter or number");
        }
        return trimmed;
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount exceeds maximum allowed");
        }
        // Ensure 2 decimal places max
        if (amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount can have at most 2 decimal places");
        }
        return amount;
    }

    private LocalDate validateDate(LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date cannot be in the future");
        }
        // Reasonable lower bound: 50 years ago
        if (date.isBefore(LocalDate.now().minusYears(50))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is too far in the past");
        }
        return date;
    }

    private CategoryEntity validateCategoryOwnership(Long categoryId, ProfileEntity profile) {
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        return categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found or not accessible"));
    }

    //helper methods
    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .notes(dto.getNotes())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .notes(entity.getNotes())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName(): "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
