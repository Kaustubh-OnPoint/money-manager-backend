package in.kaustubh.moneymanager.repository;

import in.kaustubh.moneymanager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    Optional<BudgetEntity> findByProfileIdAndCategoryIdAndMonthAndYear(
            Long profileId, Long categoryId, Integer month, Integer year);

    List<BudgetEntity> findByProfileIdAndMonthAndYear(Long profileId, Integer month, Integer year);
}
