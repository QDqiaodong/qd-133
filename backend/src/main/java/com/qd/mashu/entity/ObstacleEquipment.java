package com.qd.mashu.entity;

import com.qd.mashu.enums.TrainingLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "obstacle_equipment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_code", unique = true, nullable = false, length = 50)
    private String equipmentCode;

    @Column(name = "equipment_name", nullable = false, length = 100)
    private String equipmentName;

    @Column(name = "obstacle_height", nullable = false)
    private Double obstacleHeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "adapt_level", nullable = false, length = 20)
    private TrainingLevel adaptLevel;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}