package com.nikolaichuk.cdr.cdrbillingservice.domain.rating;

import com.nikolaichuk.cdr.cdrbillingservice.domain.cdr.UsageType;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Domain service dispatching rating to the strategy for a {@link UsageType}.
 */
public class RatingEngine {

    private final Map<UsageType, RatingStrategy> strategies;

    public RatingEngine(Collection<RatingStrategy> strategies) {
        this.strategies = new EnumMap<>(UsageType.class);
        for (RatingStrategy strategy : strategies) {
            this.strategies.put(strategy.supportedType(), strategy);
        }
    }

    public RatedCharge rate(UsageType type, RatingContext context) {
        RatingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No rating strategy for type: " + type);
        }
        return strategy.rate(context);
    }
}
