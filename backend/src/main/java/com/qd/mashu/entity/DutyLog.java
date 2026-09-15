package com.qd.mashu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班记录：每天只保留一条，记录当班教练、接班教练和今晚要留意的事项。
 * duty_date 加唯一约束，确保同一天不能重复提交。
 */
@Entity
@Table(name = "duty_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_duty_log_duty_date", columnNames = "duty_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 值班日期，一天一条 */
    @Column(name = "duty_date", nullable = false)
    private LocalDate dutyDate;

    /** 当班教练 */
    @Column(name = "on_duty_coach", nullable = false, length = 50)
    private String onDutyCoach;

    /** 接班教练 */
    @Column(name = "incoming_coach", nullable = false, length = 50)
    private String incomingCoach;

    /** 今晚要留意的事，例如哪个训练位要修、哪件器材要换 */
    @Column(name = "tonight_notes", nullable = false, length = 1000)
    private String tonightNotes;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
