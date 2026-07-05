package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.AdminTransactionDTO;
import in.kaustubh.moneymanager.dto.AdminUserDTO;
import in.kaustubh.moneymanager.entity.ExpenseEntity;
import in.kaustubh.moneymanager.entity.IncomeEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import in.kaustubh.moneymanager.repository.BudgetRepository;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import in.kaustubh.moneymanager.repository.GoalRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import in.kaustubh.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final GoalRepository goalRepository;
    private final BudgetRepository budgetRepository;

    public Map<String, Object> getPlatformStats() {
        long totalUsers = profileRepository.count();
        long totalPremiumUsers = profileRepository.countBySubscriptionTier(SubscriptionTier.PREMIUM);
        BigDecimal totalRevenue = BigDecimal.valueOf(totalPremiumUsers * 99);
        LocalDate today = LocalDate.now();
        long totalTransactionsToday = incomeRepository.countByDate(today) + expenseRepository.countByDate(today);

        BigDecimal totalIncome = incomeRepository.findAll().stream()
                .map(IncomeEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseRepository.findAll().stream()
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalGoals = goalRepository.count();
        long totalBudgets = budgetRepository.count();
        long totalTransactions = incomeRepository.count() + expenseRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPremiumUsers", totalPremiumUsers);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalTransactionsToday", totalTransactionsToday);
        stats.put("totalIncome", totalIncome);
        stats.put("totalExpense", totalExpense);
        stats.put("totalBalance", totalIncome.subtract(totalExpense));
        stats.put("totalGoals", totalGoals);
        stats.put("totalBudgets", totalBudgets);
        stats.put("totalTransactions", totalTransactions);
        return stats;
    }

    public List<AdminUserDTO> getAllUsers() {
        List<ProfileEntity> profiles = profileRepository.findAll();
        return mapUsersWithBatchStats(profiles);
    }

    public List<AdminUserDTO> getRecentUsers(int limit) {
        List<ProfileEntity> profiles = profileRepository.findAll().stream()
                .sorted(Comparator.comparing(ProfileEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
        return mapUsersWithBatchStats(profiles);
    }

    private List<AdminUserDTO> mapUsersWithBatchStats(List<ProfileEntity> profiles) {
        Map<Long, Long> incomeCounts = new HashMap<>();
        Map<Long, BigDecimal> incomeTotals = new HashMap<>();
        for (Object[] row : incomeRepository.findIncomeStatsGroupedByProfile()) {
            Long pid = (Long) row[0];
            incomeCounts.put(pid, (Long) row[1]);
            incomeTotals.put(pid, (BigDecimal) row[2]);
        }

        Map<Long, Long> expenseCounts = new HashMap<>();
        Map<Long, BigDecimal> expenseTotals = new HashMap<>();
        for (Object[] row : expenseRepository.findExpenseStatsGroupedByProfile()) {
            Long pid = (Long) row[0];
            expenseCounts.put(pid, (Long) row[1]);
            expenseTotals.put(pid, (BigDecimal) row[2]);
        }

        return profiles.stream().map(p -> {
            Long pid = p.getId();
            BigDecimal incTotal = incomeTotals.getOrDefault(pid, BigDecimal.ZERO);
            BigDecimal expTotal = expenseTotals.getOrDefault(pid, BigDecimal.ZERO);
            long incCount = incomeCounts.getOrDefault(pid, 0L);
            long expCount = expenseCounts.getOrDefault(pid, 0L);
            return AdminUserDTO.builder()
                    .id(pid)
                    .fullName(p.getFullName())
                    .email(p.getEmail())
                    .subscriptionTier(p.getSubscriptionTier())
                    .trialEndDate(p.getTrialEndDate())
                    .createdAt(p.getCreatedAt())
                    .totalTransactions(incCount + expCount)
                    .totalIncome(incTotal)
                    .totalExpense(expTotal)
                    .balance(incTotal.subtract(expTotal))
                    .role(p.getRole())
                    .isActive(p.getIsActive())
                    .lastLoginAt(p.getLastLoginAt())
                    .build();
        }).toList();
    }

    public List<AdminTransactionDTO> getAllTransactions(int limit) {
        List<AdminTransactionDTO> incomes = incomeRepository.findAll().stream().map(this::toTransactionDTO).toList();
        List<AdminTransactionDTO> expenses = expenseRepository.findAll().stream().map(this::toTransactionDTO).toList();
        return Stream.concat(incomes.stream(), expenses.stream())
                .sorted(Comparator.comparing(AdminTransactionDTO::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    public List<AdminTransactionDTO> getRecentTransactions(int limit) {
        return getAllTransactions(limit);
    }

    public List<Map<String, Object>> getMonthlyTrends() {
        LocalDate now = LocalDate.now();
        LocalDate rangeStart = now.minusMonths(11).withDayOfMonth(1);
        LocalDate rangeEnd = now.withDayOfMonth(now.lengthOfMonth());

        List<IncomeEntity> incomesInRange = incomeRepository.findByDateBetween(rangeStart, rangeEnd);
        List<ExpenseEntity> expensesInRange = expenseRepository.findByDateBetween(rangeStart, rangeEnd);

        Map<String, BigDecimal> incomeMap = incomesInRange.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().getMonth().toString().substring(0, 3),
                        LinkedHashMap::new,
                        Collectors.mapping(IncomeEntity::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        Map<String, BigDecimal> expenseMap = expensesInRange.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().getMonth().toString().substring(0, 3),
                        LinkedHashMap::new,
                        Collectors.mapping(ExpenseEntity::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            String monthLabel = monthStart.getMonth().toString().substring(0, 3);

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", monthLabel);
            point.put("income", incomeMap.getOrDefault(monthLabel, BigDecimal.ZERO));
            point.put("expense", expenseMap.getOrDefault(monthLabel, BigDecimal.ZERO));
            trends.add(point);
        }
        return trends;
    }

    public List<Map<String, Object>> getAllGoals() {
        return goalRepository.findAll().stream().map(g -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", g.getId());
            map.put("name", g.getName());
            map.put("icon", g.getIcon());
            map.put("targetAmount", g.getTargetAmount());
            map.put("currentAmount", g.getCurrentAmount());
            double pct = 0.0;
            if (g.getTargetAmount() != null && g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                pct = g.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                        .divide(g.getTargetAmount(), 1, RoundingMode.HALF_UP).doubleValue();
            }
            map.put("percentageCompleted", pct);
            map.put("deadline", g.getDeadline());
            map.put("userName", g.getProfile() != null ? g.getProfile().getFullName() : "N/A");
            map.put("createdAt", g.getCreatedAt());
            return map;
        }).toList();
    }

    public List<Map<String, Object>> getAllBudgets() {
        return budgetRepository.findAll().stream().map(b -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", b.getId());
            map.put("categoryName", b.getCategory() != null ? b.getCategory().getName() : "N/A");
            map.put("amount", b.getAmount());
            map.put("month", b.getMonth());
            map.put("year", b.getYear());
            map.put("userName", b.getProfile() != null ? b.getProfile().getFullName() : "N/A");
            return map;
        }).toList();
    }

    private AdminTransactionDTO toTransactionDTO(IncomeEntity entity) {
        return AdminTransactionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .type("income")
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .userName(entity.getProfile() != null ? entity.getProfile().getFullName() : "N/A")
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AdminTransactionDTO toTransactionDTO(ExpenseEntity entity) {
        return AdminTransactionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .type("expense")
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .userName(entity.getProfile() != null ? entity.getProfile().getFullName() : "N/A")
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
