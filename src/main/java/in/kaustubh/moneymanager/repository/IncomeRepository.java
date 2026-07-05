package in.kaustubh.moneymanager.repository;

import in.kaustubh.moneymanager.entity.IncomeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    //select * from tbl_incomes where profile_id = ?1 order by date desc
    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);

    //select * from tbl_incomes where profile_id = ?1 order by date desc limit 5
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    // Batch aggregation for admin panel (profileId, count, totalAmount)
    @Query("SELECT i.profile.id, COUNT(i), SUM(i.amount) FROM IncomeEntity i GROUP BY i.profile.id")
    List<Object[]> findIncomeStatsGroupedByProfile();

    //select * from tbl_incomes where profile_id = ?1 and date between ?2 and ?3 and name like %?4%
    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
            Long profileId,
            LocalDate startDate,
            LocalDate endDate,
            String keyword,
            Sort sort
    );

    //select * from tbl_incomes where profile_id = ?1 and date between ?2 and ?3
    List<IncomeEntity> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

    long countByCategoryId(Long categoryId);

    long countByProfileId(Long profileId);

    long countByProfileIdAndDate(Long profileId, java.time.LocalDate date);

    long countByDate(java.time.LocalDate date);

    // Optimized batch query for monthly trends
    List<IncomeEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId AND i.date BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByProfileIdAndDateBetween(
            @Param("profileId") Long profileId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);
}
