package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.ExpenseDTO;
import in.kaustubh.moneymanager.dto.IncomeDTO;
import in.kaustubh.moneymanager.entity.RecurringTransactionEntity;
import in.kaustubh.moneymanager.enums.TransactionType;
import in.kaustubh.moneymanager.repository.RecurringTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringJobService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @Scheduled(cron = "0 0 1 * * *", zone = "IST")
    @Transactional
    public void processRecurringTransactions() {
        log.info("Job started: processRecurringTransactions()");
        LocalDate today = LocalDate.now();
        List<RecurringTransactionEntity> dueTransactions = recurringTransactionRepository
                .findByNextExecutionDateLessThanEqualAndIsActiveTrue(today);

        for (RecurringTransactionEntity recurring : dueTransactions) {
            try {
                if (recurring.getType() == TransactionType.INCOME) {
                    IncomeDTO dto = IncomeDTO.builder()
                            .name(recurring.getName())
                            .amount(recurring.getAmount())
                            .icon(recurring.getIcon())
                            .categoryId(recurring.getCategory().getId())
                            .date(today)
                            .build();
                    incomeService.addIncome(dto);
                } else {
                    ExpenseDTO dto = ExpenseDTO.builder()
                            .name(recurring.getName())
                            .amount(recurring.getAmount())
                            .icon(recurring.getIcon())
                            .categoryId(recurring.getCategory().getId())
                            .date(today)
                            .build();
                    expenseService.addExpense(dto);
                }
                recurring.setNextExecutionDate(today.plusMonths(1));
                recurringTransactionRepository.save(recurring);
                log.info("Processed recurring transaction id={} for user id={}", recurring.getId(), recurring.getProfile().getId());
            } catch (Exception e) {
                log.error("Failed to process recurring transaction id={}: {}", recurring.getId(), e.getMessage());
            }
        }
        log.info("Job completed: processRecurringTransactions()");
    }
}
