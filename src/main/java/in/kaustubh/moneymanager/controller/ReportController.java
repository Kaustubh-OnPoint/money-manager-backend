package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.ExpenseRepository;
import in.kaustubh.moneymanager.repository.IncomeRepository;
import in.kaustubh.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalIncome = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;

        BigDecimal totalExpense = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        // Note: weekly breakdown and category breakdown would need more detailed queries
        // For now, returning the basic report
        return ResponseEntity.ok(Map.of(
                "totalIncome", totalIncome,
                "totalExpense", totalExpense,
                "netSavings", netSavings,
                "month", month,
                "year", year
        ));
    }
}
