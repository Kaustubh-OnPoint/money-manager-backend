package in.kaustubh.moneymanager.controller;

import in.kaustubh.moneymanager.dto.PaymentVerifyDTO;
import in.kaustubh.moneymanager.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder() throws Exception {
        Map<String, String> order = paymentService.createOrder();
        return ResponseEntity.ok(order);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyPayment(@RequestBody PaymentVerifyDTO dto) throws Exception {
        paymentService.verifyPayment(dto.getRazorpayPaymentId(), dto.getRazorpayOrderId(), dto.getRazorpaySignature());
        return ResponseEntity.ok().build();
    }
}
