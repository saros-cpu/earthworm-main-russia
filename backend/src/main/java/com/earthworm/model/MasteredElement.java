package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mastered_elements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MasteredElement {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(nullable = false, columnDefinition = "JSON")
    private String content;

    @Column(name = "mastered_at", insertable = false, updatable = false)
    private LocalDateTime masteredAt;
}
