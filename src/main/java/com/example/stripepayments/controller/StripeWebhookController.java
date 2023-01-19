package com.example.stripepayments.controller;

import com.stripe.model.Event;
import com.stripe.model.EventDataDeserializer;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/stripe/events")
    public String handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Webhook received");
        if(sigHeader == null){
            return "";
        }
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            log.error("Error while constructing event", e);
            return "";
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if(dataObjectDeserializer.getObject().isPresent()){
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                log.info("PaymentIntent was successful!");
                break;
            case "payment_method.attached":
                log.info("PaymentMethod was attached to a Customer!");
                break;
            // ... handle other event types
            default:
                log.warn("Unhandled event type: {}", event.getType());
                break;
        }
        return "";
    }
}
