package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.GoalDTO;
import in.kaustubh.moneymanager.entity.GoalEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final ProfileService profileService;

    public GoalDTO createGoal(GoalDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal name is required");
        }
        if (dto.getName().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal name must not exceed 100 characters");
        }
        if (dto.getTargetAmount() == null || dto.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target amount must be greater than 0");
        }
        if (dto.getTargetAmount().compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target amount exceeds maximum allowed");
        }
        if (dto.getCurrentAmount() != null && dto.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current amount cannot be negative");
        }

        GoalEntity entity = GoalEntity.builder()
                .profile(profile)
                .name(dto.getName().trim())
                .icon(dto.getIcon())
                .targetAmount(dto.getTargetAmount())
                .currentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO)
                .deadline(dto.getDeadline())
                .build();

        entity = goalRepository.save(entity);
        return toDTO(entity);
    }

    public List<GoalDTO> getGoalsForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return goalRepository.findByProfileId(profile.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public GoalDTO updateGoal(Long id, GoalDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        GoalEntity entity = goalRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"));

        if (dto.getName() != null) {
            if (dto.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal name is required");
            }
            if (dto.getName().length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal name must not exceed 100 characters");
            }
            entity.setName(dto.getName().trim());
        }
        if (dto.getIcon() != null) {
            entity.setIcon(dto.getIcon());
        }
        if (dto.getTargetAmount() != null) {
            if (dto.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target amount must be greater than 0");
            }
            entity.setTargetAmount(dto.getTargetAmount());
        }
        if (dto.getCurrentAmount() != null) {
            if (dto.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current amount cannot be negative");
            }
            entity.setCurrentAmount(dto.getCurrentAmount());
        }
        if (dto.getDeadline() != null) {
            entity.setDeadline(dto.getDeadline());
        }

        entity = goalRepository.save(entity);
        return toDTO(entity);
    }

    public void deleteGoal(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        GoalEntity entity = goalRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"));
        goalRepository.delete(entity);
    }

    private GoalDTO toDTO(GoalEntity entity) {
        double percentage = 0.0;
        if (entity.getTargetAmount() != null && entity.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = entity.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                    .divide(entity.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return GoalDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .targetAmount(entity.getTargetAmount())
                .currentAmount(entity.getCurrentAmount())
                .deadline(entity.getDeadline())
                .percentageCompleted(percentage)
                .build();
    }
}
