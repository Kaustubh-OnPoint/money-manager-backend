package in.kaustubh.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionDTO {
    private Long id;
    private String name;
    private String icon;
    private BigDecimal amount;
    private LocalDate date;
    private String type;
    private String categoryName;
    private String userName;
    private String notes;
    private LocalDateTime createdAt;
}
