package in.kaustubh.moneymanager.config;

import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.Role;
import in.kaustubh.moneymanager.enums.SubscriptionStatus;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@moneymanager.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void run(String... args) {
        if (profileRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            ProfileEntity admin = ProfileEntity.builder()
                    .fullName("Admin")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(Role.ADMIN)
                    .subscriptionTier(SubscriptionTier.PREMIUM)
                    .subscriptionStatus(SubscriptionStatus.ACTIVE)
                    .isActive(true)
                    .trialStartDate(LocalDateTime.now())
                    .trialEndDate(LocalDateTime.now().plusYears(100))
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            admin = profileRepository.save(admin);

            List<CategoryEntity> defaults = List.of(
                CategoryEntity.builder().name("Salary").type("income").icon("💰").profile(admin).build(),
                CategoryEntity.builder().name("Freelance").type("income").icon("💻").profile(admin).build(),
                CategoryEntity.builder().name("Investments").type("income").icon("📈").profile(admin).build(),
                CategoryEntity.builder().name("Gifts").type("income").icon("🎁").profile(admin).build(),
                CategoryEntity.builder().name("Other Income").type("income").icon("💵").profile(admin).build(),
                CategoryEntity.builder().name("Food & Dining").type("expense").icon("🍔").profile(admin).build(),
                CategoryEntity.builder().name("Rent / Housing").type("expense").icon("🏠").profile(admin).build(),
                CategoryEntity.builder().name("Transportation").type("expense").icon("🚗").profile(admin).build(),
                CategoryEntity.builder().name("Utilities").type("expense").icon("💡").profile(admin).build(),
                CategoryEntity.builder().name("Entertainment").type("expense").icon("🎬").profile(admin).build(),
                CategoryEntity.builder().name("Shopping").type("expense").icon("🛍️").profile(admin).build(),
                CategoryEntity.builder().name("Health & Medical").type("expense").icon("🏥").profile(admin).build(),
                CategoryEntity.builder().name("Other Expense").type("expense").icon("📄").profile(admin).build()
            );
            categoryRepository.saveAll(defaults);

            log.info("Admin user seeded successfully: {}", ADMIN_EMAIL);
        } else {
            log.info("Admin user already exists, skipping seed.");
        }

        // Backfill default categories for existing users who have none
        List<ProfileEntity> allUsers = profileRepository.findAll();
        for (ProfileEntity user : allUsers) {
            long categoryCount = categoryRepository.countByProfileId(user.getId());
            if (categoryCount == 0) {
                List<CategoryEntity> defaults = List.of(
                    CategoryEntity.builder().name("Salary").type("income").icon("💰").profile(user).build(),
                    CategoryEntity.builder().name("Freelance").type("income").icon("💻").profile(user).build(),
                    CategoryEntity.builder().name("Investments").type("income").icon("📈").profile(user).build(),
                    CategoryEntity.builder().name("Gifts").type("income").icon("🎁").profile(user).build(),
                    CategoryEntity.builder().name("Other Income").type("income").icon("💵").profile(user).build(),
                    CategoryEntity.builder().name("Food & Dining").type("expense").icon("🍔").profile(user).build(),
                    CategoryEntity.builder().name("Rent / Housing").type("expense").icon("🏠").profile(user).build(),
                    CategoryEntity.builder().name("Transportation").type("expense").icon("🚗").profile(user).build(),
                    CategoryEntity.builder().name("Utilities").type("expense").icon("💡").profile(user).build(),
                    CategoryEntity.builder().name("Entertainment").type("expense").icon("🎬").profile(user).build(),
                    CategoryEntity.builder().name("Shopping").type("expense").icon("🛍️").profile(user).build(),
                    CategoryEntity.builder().name("Health & Medical").type("expense").icon("🏥").profile(user).build(),
                    CategoryEntity.builder().name("Other Expense").type("expense").icon("📄").profile(user).build()
                );
                categoryRepository.saveAll(defaults);
                log.info("Seeded default categories for user: {}", user.getEmail());
            }
        }
    }
}
