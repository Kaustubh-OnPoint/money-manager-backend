package in.kaustubh.moneymanager.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import in.kaustubh.moneymanager.entity.ProfileEntity;
import in.kaustubh.moneymanager.enums.SubscriptionStatus;
import in.kaustubh.moneymanager.enums.SubscriptionTier;
import in.kaustubh.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Map<String, String> createOrder() throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        ProfileEntity profile = profileService.getCurrentProfile();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 9900); // Rs 99 in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + profile.getId());

        Order order = razorpay.orders.create(orderRequest);
        return Map.of(
                "orderId", order.get("id"),
                "amount", order.get("amount").toString(),
                "currency", order.get("currency"),
                "key", razorpayKeyId
        );
    }

    public void verifyPayment(String paymentId, String orderId, String signature) throws Exception {
        String payload = orderId + "|" + paymentId;
        boolean isValid = Utils.verifySignature(payload, signature, razorpayKeySecret);
        if (!isValid) {
            throw new RuntimeException("Invalid payment signature");
        }

        ProfileEntity profile = profileService.getCurrentProfile();
        profile.setSubscriptionTier(SubscriptionTier.PREMIUM);
        profile.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        profile.setRazorpayPaymentId(paymentId);
        profileRepository.save(profile);
        log.info("User {} upgraded to PREMIUM with payment {}", profile.getEmail(), paymentId);
    }
}
