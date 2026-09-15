package com.qd.mashu.service;

import com.qd.mashu.dto.request.EquipmentRepairReturnRequest;
import com.qd.mashu.dto.request.EquipmentRepairSendRequest;
import com.qd.mashu.dto.response.EquipmentRepairOrderResponse;
import com.qd.mashu.entity.EquipmentRepairOrder;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.EquipmentRepairOrderRepository;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 障碍杆送修两阶段流程单测：
 * 开单必填故障说明和经办教练，并拆下训练位；归还必填修复结论后才恢复在用。
 */
@ExtendWith(MockitoExtension.class)
class EquipmentRepairServiceTest {

    @Mock
    private EquipmentRepairOrderRepository repairOrderRepository;

    @Mock
    private ObstacleEquipmentRepository equipmentRepository;

    @Mock
    private ObstacleEquipmentService equipmentService;

    @Mock
    private LevelCacheService levelCacheService;

    @InjectMocks
    private EquipmentRepairService repairService;

    private ObstacleEquipment equipment(long id, int status) {
        return ObstacleEquipment.builder()
                .id(id)
                .equipmentCode("EQ001")
                .equipmentName("初级障碍杆A组")
                .obstacleHeight(40.0)
                .adaptLevel(TrainingLevel.LEVEL_1)
                .status(status)
                .build();
    }

    private EquipmentRepairSendRequest sendRequest() {
        return EquipmentRepairSendRequest.builder()
                .faultDescription("横杆开裂")
                .handlerCoach("陈教练")
                .build();
    }

    @Test
    void send_requires_fault_description_and_handler() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.sendForRepair(1L,
                        EquipmentRepairSendRequest.builder().handlerCoach("陈教练").build()));
        assertTrue(ex.getMessage().contains("故障说明"));
        verify(equipmentRepository, never()).findWithLockById(any());
        verify(repairOrderRepository, never()).save(any());
    }

    @Test
    void send_detaches_station_and_marks_equipment_in_repair() {
        ObstacleEquipment pole = equipment(7L, 1);
        when(equipmentRepository.findWithLockById(7L)).thenReturn(Optional.of(pole));
        when(equipmentService.detachFromStations(pole)).thenReturn(1);
        when(repairOrderRepository.save(any(EquipmentRepairOrder.class))).thenAnswer(invocation -> {
            EquipmentRepairOrder order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        EquipmentRepairOrderResponse response = repairService.sendForRepair(7L, sendRequest());

        assertEquals(100L, response.getId());
        assertEquals("IN_REPAIR", response.getStatus());
        assertEquals("横杆开裂", response.getFaultDescription());
        assertEquals("陈教练", response.getHandlerCoach());
        assertEquals(2, pole.getStatus());
        // 不是只改器材名单：开单事务里必须先调用拆下训练位
        verify(equipmentService).detachFromStations(pole);
        verify(equipmentRepository).save(pole);
        verify(levelCacheService).refreshLevelCache();
    }

    @Test
    void send_rejects_when_already_in_repair() {
        ObstacleEquipment pole = equipment(7L, 2);
        when(equipmentRepository.findWithLockById(7L)).thenReturn(Optional.of(pole));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.sendForRepair(7L, sendRequest()));

        assertTrue(ex.getMessage().contains("已在修中"));
        verify(equipmentService, never()).detachFromStations(any());
        verify(repairOrderRepository, never()).save(any());
        verify(equipmentRepository, never()).save(any(ObstacleEquipment.class));
    }

    @Test
    void return_requires_repair_conclusion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.returnFromRepair(100L,
                        EquipmentRepairReturnRequest.builder().repairConclusion(" ").build()));

        assertTrue(ex.getMessage().contains("修复结论"));
        verify(repairOrderRepository, never()).save(any());
        verify(equipmentRepository, never()).save(any(ObstacleEquipment.class));
    }

    @Test
    void return_with_conclusion_restores_equipment_to_active() {
        ObstacleEquipment pole = equipment(7L, 2);
        EquipmentRepairOrder order = EquipmentRepairOrder.builder()
                .id(100L)
                .equipment(pole)
                .faultDescription("横杆开裂")
                .handlerCoach("陈教练")
                .status("IN_REPAIR")
                .build();
        when(repairOrderRepository.findWithLockById(100L)).thenReturn(Optional.of(order));
        when(equipmentRepository.findWithLockById(7L)).thenReturn(Optional.of(pole));
        when(repairOrderRepository.save(order)).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentRepairOrderResponse response = repairService.returnFromRepair(100L,
                EquipmentRepairReturnRequest.builder().repairConclusion("已更换开裂横杆并复检").build());

        assertEquals("RETURNED", response.getStatus());
        assertEquals("已更换开裂横杆并复检", response.getRepairConclusion());
        assertNotNull(response.getReturnedTime());
        assertEquals(1, pole.getStatus());
        verify(equipmentRepository).save(pole);
        verify(levelCacheService).refreshLevelCache();
    }

    @Test
    void bindable_validation_rejects_in_repair_equipment() {
        ObstacleEquipment pole = equipment(7L, 2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repairService.validateCanBind(pole));

        assertTrue(ex.getMessage().contains("正在送修"));
    }
}
