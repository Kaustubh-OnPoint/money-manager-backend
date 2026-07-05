package in.kaustubh.moneymanager.repository;

import in.kaustubh.moneymanager.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    List<GoalEntity> findByProfileId(Long profileId);

    Optional<GoalEntity> findByIdAndProfileId(Long id, Long profileId);
}
