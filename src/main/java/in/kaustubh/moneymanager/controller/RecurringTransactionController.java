package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.dto.RecurringTransactionDTO;
import in.kaustubh.moneymanager.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recurring")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionDTO> createRecurring(@RequestBody RecurringTransactionDTO dto) {
        RecurringTransactionDTO saved = recurringTransactionService.createRecurring(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionDTO>> getRecurring() {
        List<RecurringTransactionDTO> list = recurringTransactionService.getRecurringForCurrentUser();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelRecurring(@PathVariable Long id) {
        recurringTransactionService.cancelRecurring(id);
        return ResponseEntity.noContent().build();
    }
}
