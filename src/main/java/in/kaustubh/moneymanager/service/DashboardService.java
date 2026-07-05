package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.ExpenseDTO;
import in.kaustubh.moneymanager.dto.IncomeDTO;
import in.kaustubh.moneymanager.dto.RecentTransactionDTO;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions = concat(latestIncomes.stream().map(income ->
                        RecentTransactionDTO.builder()
                                .id(income.getId())
                                .profileId(profile.getId())
                                .icon(income.getIcon())
                                .name(income.getName())
                                .amount(income.getAmount())
                                .date(income.getDate())
                                .createdAt(income.getCreatedAt())
                                .updatedAt(income.getUpdatedAt())
                                .type("income")
                                .build()),
                latestExpenses.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .profileId(profile.getId())
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build()))
                .sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).collect(Collectors.toList());
        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentUser();
        BigDecimal totalExpense = expenseService.getTotalExpenseForCurrentUser();

        // Last month comparison
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());
        BigDecimal lastMonthIncome = incomeRepository.sumAmountByProfileIdAndDateBetween(profile.getId(), lastMonthStart, lastMonthEnd);
        BigDecimal lastMonthExpense = expenseRepository.sumAmountByProfileIdAndDateBetween(profile.getId(), lastMonthStart, lastMonthEnd);
        if (lastMonthIncome == null) lastMonthIncome = BigDecimal.ZERO;
        if (lastMonthExpense == null) lastMonthExpense = BigDecimal.ZERO;
        BigDecimal lastMonthBalance = lastMonthIncome.subtract(lastMonthExpense);

        returnValue.put("totalBalance", totalIncome.subtract(totalExpense));
        returnValue.put("totalIncome", totalIncome);
        returnValue.put("totalExpense", totalExpense);
        returnValue.put("lastMonthBalance", lastMonthBalance);
        returnValue.put("lastMonthIncome", lastMonthIncome);
        returnValue.put("lastMonthExpense", lastMonthExpense);
        returnValue.put("incomeChangePercent", calcChangePercent(lastMonthIncome, totalIncome));
        returnValue.put("expenseChangePercent", calcChangePercent(lastMonthExpense, totalExpense));
        returnValue.put("balanceChangePercent", calcChangePercent(lastMonthBalance, totalIncome.subtract(totalExpense)));
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions);
        return returnValue;
    }

    private Double calcChangePercent(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
