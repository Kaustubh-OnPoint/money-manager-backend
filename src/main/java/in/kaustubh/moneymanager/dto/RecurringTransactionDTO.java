package in.kaustubh.moneymanager.dto;

import in.kaustubh.moneymanager.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionDTO {

    private Long id;
    private String name;
    private BigDecimal amount;
    private String icon;
    private Long categoryId;
    private String categoryName;
    private TransactionType type;
    private LocalDate nextExecutionDate;
    private Boolean isActive;
}
