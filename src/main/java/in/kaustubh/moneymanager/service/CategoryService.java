package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.CategoryDTO;
import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import in.kaustubh.moneymanager.util.SubscriptionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SubscriptionChecker subscriptionChecker;

    private static final int CATEGORY_NAME_MAX_LENGTH = 50;
    private static final Set<String> VALID_TYPES = Set.of("income", "expense");

    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        subscriptionChecker.checkCanAddCategory(profile);

        String sanitizedName = validateAndSanitizeName(categoryDTO.getName());
        validateType(categoryDTO.getType());

        if (categoryRepository.existsByNameIgnoreCaseAndProfileId(sanitizedName, profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A category with this name already exists");
        }

        CategoryEntity newCategory = CategoryEntity.builder()
                .name(sanitizedName)
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType().toLowerCase().trim())
                .build();
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    //get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    //get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or not accessible"));

        String sanitizedName = validateAndSanitizeName(dto.getName());

        // Check for duplicate name on update (excluding current category itself)
        categoryRepository.findByNameIgnoreCaseAndProfileId(sanitizedName, profile.getId())
                .ifPresent(duplicate -> {
                    if (!duplicate.getId().equals(categoryId)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "A category with this name already exists");
                    }
                });

        existingCategory.setName(sanitizedName);
        existingCategory.setIcon(dto.getIcon());
        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    public void deleteCategory(Long categoryId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or not accessible"));
        long incomeCount = incomeRepository.countByCategoryId(categoryId);
        long expenseCount = expenseRepository.countByCategoryId(categoryId);
        if (incomeCount > 0 || expenseCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete: category has transactions. Reassign them first.");
        }
        categoryRepository.delete(category);
    }

    // Validation helpers
    private String validateAndSanitizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        String trimmed = rawName.trim();
        if (trimmed.length() > CATEGORY_NAME_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must be at most " + CATEGORY_NAME_MAX_LENGTH + " characters");
        }
        // Disallow names that are only whitespace or special characters without letters/digits
        if (!trimmed.matches(".*[\\p{L}\\d].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must contain at least one letter or number");
        }
        return trimmed;
    }

    private void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type is required");
        }
        if (!VALID_TYPES.contains(type.toLowerCase().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must be 'income' or 'expense'");
        }
    }

    //helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ?  entity.getProfile().getId(): null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();

    }
}
