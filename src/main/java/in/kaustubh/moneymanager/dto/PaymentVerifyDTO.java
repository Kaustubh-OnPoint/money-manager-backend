package in.kaustubh.moneymanager.dto;

import lombok.Data;

@Data
public class PaymentVerifyDTO {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
}
