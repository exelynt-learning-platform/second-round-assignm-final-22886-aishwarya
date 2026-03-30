package com.ecommerce.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.enabled:false}")
    private boolean stripeEnabled;

    @PostConstruct
    public void init() {
        if (stripeEnabled && stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe payment gateway configured successfully");
        } else {
            logger.warn("Stripe is NOT configured. Payment endpoints will return an error. "
                    + "To enable: set stripe.secret-key and stripe.enabled=true in application.properties");
        }
    }

    public boolean isStripeConfigured() {
        return stripeEnabled && stripeSecretKey != null && !stripeSecretKey.isBlank();
    }
}
