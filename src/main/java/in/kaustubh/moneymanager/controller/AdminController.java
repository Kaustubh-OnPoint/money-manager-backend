package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.dto.AdminTransactionDTO;
import in.kaustubh.moneymanager.dto.AdminUserDTO;
import in.kaustubh.moneymanager.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(@RequestParam(defaultValue = "1000") int limit) {
        List<AdminUserDTO> users = adminService.getAllUsers();
        if (users.size() > limit) {
            users = users.subList(0, limit);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/recent")
    public ResponseEntity<List<AdminUserDTO>> getRecentUsers(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminService.getRecentUsers(limit));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionDTO>> getAllTransactions(@RequestParam(defaultValue = "500") int limit) {
        return ResponseEntity.ok(adminService.getAllTransactions(limit));
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<AdminTransactionDTO>> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getRecentTransactions(limit));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends() {
        return ResponseEntity.ok(adminService.getMonthlyTrends());
    }

    @GetMapping("/goals")
    public ResponseEntity<List<Map<String, Object>>> getAllGoals(@RequestParam(defaultValue = "500") int limit) {
        List<Map<String, Object>> goals = adminService.getAllGoals();
        if (goals.size() > limit) {
            goals = goals.subList(0, limit);
        }
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<Map<String, Object>>> getAllBudgets(@RequestParam(defaultValue = "500") int limit) {
        List<Map<String, Object>> budgets = adminService.getAllBudgets();
        if (budgets.size() > limit) {
            budgets = budgets.subList(0, limit);
        }
        return ResponseEntity.ok(budgets);
    }
}
