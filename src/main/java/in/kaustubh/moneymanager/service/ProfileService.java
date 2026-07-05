package in.kaustubh.moneymanager.service;

import in.kaustubh.moneymanager.dto.AuthDTO;
import in.kaustubh.moneymanager.dto.ProfileDTO;
import in.kaustubh.moneymanager.entity.CategoryEntity;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.repository.CategoryRepository;
import in.kaustubh.moneymanager.repository.ProfileRepository;
import in.kaustubh.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    @Value("${money.manager.frontend.url}")
    private String frontendURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        seedDefaultCategories(newProfile);

        String activationLink = frontendURL + "/account-activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Money Manager account";
        String htmlBody = buildActivationEmail(newProfile.getFullName(), activationLink);
        emailService.sendHtmlEmail(newProfile.getEmail(), subject, htmlBody);
        return toDTO(newProfile);
    }

    private String buildActivationEmail(String fullName, String activationLink) {
        String name = fullName != null && !fullName.isBlank() ? fullName : "there";
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Activate Your Account</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding:0;background-color:#f3f4f8;font-family:'Segoe UI',Arial,sans-serif;\">\n" +
                "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\" style=\"padding:40px 20px;\">\n" +
                "        <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width:600px;width:100%;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);\">\n" +
                "          <tr>\n" +
                "            <td style=\"background:linear-gradient(135deg,#7c3aed 0%,#6d28d9 100%);padding:40px 30px;text-align:center;\">\n" +
                "              <h1 style=\"margin:0;color:#ffffff;font-size:24px;font-weight:600;\">Money Manager</h1>\n" +
                "              <p style=\"margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:14px;\">Smart finance. Simple life.</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <tr>\n" +
                "            <td style=\"padding:40px 30px;text-align:center;\">\n" +
                "              <h2 style=\"margin:0 0 16px;color:#1f2937;font-size:22px;font-weight:600;\">Welcome aboard, " + name + "!</h2>\n" +
                "              <p style=\"margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.6;\">\n" +
                "                Thank you for signing up. Please verify your email address to activate your account and start managing your finances.\n" +
                "              </p>\n" +
                "              <a href=\"" + activationLink + "\" style=\"display:inline-block;background:linear-gradient(135deg,#7c3aed 0%,#6d28d9 100%);color:#ffffff;text-decoration:none;padding:14px 36px;border-radius:10px;font-size:15px;font-weight:600;\">Activate Account</a>\n" +
                "              <p style=\"margin:28px 0 0;color:#9ca3af;font-size:13px;\">\n" +
                "                Or copy and paste this link into your browser:<br>\n" +
                "                <span style=\"color:#7c3aed;word-break:break-all;\">" + activationLink + "</span>\n" +
                "              </p>\n" +
                "              <p style=\"margin:24px 0 0;color:#9ca3af;font-size:12px;\">\n" +
                "                This link will expire in 24 hours. If you did not create this account, you can safely ignore this email.\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "          <tr>\n" +
                "            <td style=\"padding:20px 30px;background:#f9fafb;text-align:center;border-top:1px solid #e5e7eb;\">\n" +
                "              <p style=\"margin:0;color:#9ca3af;font-size:12px;\">\n" +
                "                &copy; " + java.time.Year.now().getValue() + " Money Manager. All rights reserved.\n" +
                "              </p>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private void seedDefaultCategories(ProfileEntity profile) {
        List<CategoryEntity> defaults = List.of(
            CategoryEntity.builder().name("Salary").type("income").icon("💰").profile(profile).build(),
            CategoryEntity.builder().name("Freelance").type("income").icon("💻").profile(profile).build(),
            CategoryEntity.builder().name("Investments").type("income").icon("📈").profile(profile).build(),
            CategoryEntity.builder().name("Gifts").type("income").icon("🎁").profile(profile).build(),
            CategoryEntity.builder().name("Other Income").type("income").icon("💵").profile(profile).build(),
            CategoryEntity.builder().name("Food & Dining").type("expense").icon("🍔").profile(profile).build(),
            CategoryEntity.builder().name("Rent / Housing").type("expense").icon("🏠").profile(profile).build(),
            CategoryEntity.builder().name("Transportation").type("expense").icon("🚗").profile(profile).build(),
            CategoryEntity.builder().name("Utilities").type("expense").icon("💡").profile(profile).build(),
            CategoryEntity.builder().name("Entertainment").type("expense").icon("🎬").profile(profile).build(),
            CategoryEntity.builder().name("Shopping").type("expense").icon("🛍️").profile(profile).build(),
            CategoryEntity.builder().name("Health & Medical").type("expense").icon("🏥").profile(profile).build(),
            CategoryEntity.builder().name("Other Expense").type("expense").icon("📄").profile(profile).build()
        );
        categoryRepository.saveAll(defaults);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .role(profileEntity.getRole())
                .subscriptionTier(profileEntity.getSubscriptionTier())
                .trialEndDate(profileEntity.getTrialEndDate())
                .subscriptionStatus(profileEntity.getSubscriptionStatus())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .lastLoginAt(profileEntity.getLastLoginAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return toDTO(currentUser);
    }

    public ProfileDTO updateProfile(ProfileDTO profileDTO) {
        ProfileEntity currentUser = getCurrentProfile();
        if (profileDTO.getFullName() != null && !profileDTO.getFullName().isBlank()) {
            currentUser.setFullName(profileDTO.getFullName());
        }
        if (profileDTO.getProfileImageUrl() != null) {
            currentUser.setProfileImageUrl(profileDTO.getProfileImageUrl());
        }
        currentUser = profileRepository.save(currentUser);
        return toDTO(currentUser);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            ProfileEntity profile = profileRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found"));
            boolean isFirstLogin = profile.getLastLoginAt() == null;
            profile.setLastLoginAt(java.time.LocalDateTime.now());
            profileRepository.save(profile);
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail()),
                    "isFirstLogin", isFirstLogin
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
