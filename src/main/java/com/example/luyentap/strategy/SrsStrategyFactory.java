package com.example.luyentap.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory để lấy Strategy tương ứng theo tên (SM-2, LEITNER)
 * Áp dụng Factory Method & Strategy Pattern
 */
@Component
public class SrsStrategyFactory {

    private final Map<String, SpacedRepetitionStrategy> strategyMap;

    public SrsStrategyFactory(List<SpacedRepetitionStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getStrategyName().toUpperCase(),
                        Function.identity()
                ));
    }

    public SpacedRepetitionStrategy getStrategy(String strategyName) {
        if (strategyName == null || strategyName.isBlank()) {
            return strategyMap.getOrDefault(Sm2SpacedRepetitionStrategy.STRATEGY_NAME,
                    strategyMap.values().iterator().next());
        }
        SpacedRepetitionStrategy strategy = strategyMap.get(strategyName.trim().toUpperCase());
        if (strategy == null) {
            return strategyMap.get(Sm2SpacedRepetitionStrategy.STRATEGY_NAME);
        }
        return strategy;
    }
}
