package com.nikolaichuk.cdr.cdrbillingservice.application.config;

import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingEngine;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.RatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.CallRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.DataRatingStrategy;
import com.nikolaichuk.cdr.cdrbillingservice.domain.rating.strategy.SmsRatingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for rating strategies and engine.
 */
@Configuration
public class RatingConfiguration {

    @Bean
    CallRatingStrategy callRatingStrategy() {
        return new CallRatingStrategy();
    }

    @Bean
    SmsRatingStrategy smsRatingStrategy() {
        return new SmsRatingStrategy();
    }

    @Bean
    DataRatingStrategy dataRatingStrategy() {
        return new DataRatingStrategy();
    }

    @Bean
    RatingEngine ratingEngine(List<RatingStrategy> strategies) {
        return new RatingEngine(strategies);
    }
}
