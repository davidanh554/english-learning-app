package com.example.luyentap.event;

import com.example.luyentap.strategy.ReviewRating;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event phát ra khi người học hoàn thành một lượt ôn thẻ từ vựng
 * Sử dụng cơ chế Spring ApplicationEvent (In-process Event-Driven)
 */
@Getter
public class VocabularyStudiedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long vocabularyId;
    private final Long topicId;
    private final ReviewRating rating;
    private final int xpEarned;
    private final boolean mastered;
    private final LocalDateTime studiedAt;

    public VocabularyStudiedEvent(Object source, Long userId, Long vocabularyId, Long topicId,
                                  ReviewRating rating, boolean mastered) {
        super(source);
        this.userId = userId;
        this.vocabularyId = vocabularyId;
        this.topicId = topicId;
        this.rating = rating;
        this.xpEarned = rating != null ? rating.getXpReward() : 5;
        this.mastered = mastered;
        this.studiedAt = LocalDateTime.now();
    }
}
