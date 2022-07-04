package com.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private final Long orderId;
    private final String creditCardNumber;
}
