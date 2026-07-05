package in.kaustubh.moneymanager.repository;

import in.kaustubh.moneymanager.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, Long> {

    List<RecurringTransactionEntity> findByProfileIdAndIsActiveTrue(Long profileId);

    List<RecurringTransactionEntity> findByNextExecutionDateLessThanEqualAndIsActiveTrue(LocalDate date);
}
