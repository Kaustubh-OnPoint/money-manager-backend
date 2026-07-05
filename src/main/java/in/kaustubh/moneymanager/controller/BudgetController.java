package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.dto.BudgetDTO;
import in.kaustubh.moneymanager.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetDTO> saveOrUpdateBudget(@RequestBody BudgetDTO dto) {
        BudgetDTO saved = budgetService.saveOrUpdateBudget(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<BudgetDTO> budgets = budgetService.getBudgetsForMonth(month, year);
        return ResponseEntity.ok(budgets);
    }
}
