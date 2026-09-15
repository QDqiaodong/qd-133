package com.qd.mashu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 障碍杆送修单。
 *
 * <p>送修和归还是两个必须分开提交的步骤：
 * <ul>
 *     <li>送修时记录故障说明、经办教练，并先把杆从训练位拆下，器材状态改为在修；</li>
 *     <li>修好归还时必须填写修复结论，确认后器材才能恢复成在用。</li>
 * </ul>
 */
@Entity
@Table(name = "equipment_repair_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private ObstacleEquipment equipment;

    /** 故障说明：开单必填 */
    @Column(name = "fault_description", nullable = false, length = 500)
    private String faultDescription;

    /** 经办教练：开单必填 */
    @Column(name = "handler_coach", nullable = false, length = 100)
    private String handlerCoach;

    /** IN_REPAIR-在修中，RETURNED-已修好归还 */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "IN_REPAIR";

    /** 修复结论：归还必填，未填写不能恢复成在用 */
    @Column(name = "repair_conclusion", length = 500)
    private String repairConclusion;

    @Column(name = "sent_time", nullable = false)
    @Builder.Default
    private LocalDateTime sentTime = LocalDateTime.now();

    @Column(name = "returned_time")
    private LocalDateTime returnedTime;

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
