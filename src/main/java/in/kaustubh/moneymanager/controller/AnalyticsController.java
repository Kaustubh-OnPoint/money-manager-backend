package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ProfileService profileService;
    private final DataSource dataSource;

    @GetMapping("/category-spending")
    public ResponseEntity<List<Map<String, Object>>> getCategorySpending(
            @RequestParam Integer month,
            @RequestParam Integer year) throws Exception {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        String sql = "SELECT c.name as category_name, SUM(e.amount) as total_amount " +
                "FROM tbl_expenses e JOIN tbl_categories c ON e.category_id = c.id " +
                "WHERE e.profile_id = ? AND e.date BETWEEN ? AND ? " +
                "GROUP BY c.name ORDER BY total_amount DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, profile.getId());
            ps.setObject(2, startDate);
            ps.setObject(3, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(Map.of(
                            "categoryName", rs.getString("category_name"),
                            "amount", rs.getBigDecimal("total_amount")
                    ));
                }
            }
        }
        return ResponseEntity.ok(result);
    }
}
