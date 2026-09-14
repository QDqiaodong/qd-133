package com.qd.mashu.entity;

import com.qd.mashu.enums.TrainingLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "level_change_log", uniqueConstraints = {
        // 骑手等级只升不降，同一骑手同一新等级只会合法出现一次；
        // 唯一约束从数据库层挡住并发双击留下的重复变更记录
        @UniqueConstraint(name = "uk_level_change_rider_new_level", columnNames = {"rider_id", "new_level"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_level", nullable = false, length = 20)
    private TrainingLevel previousLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_level", nullable = false, length = 20)
    private TrainingLevel newLevel;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}