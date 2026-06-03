package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_tasks")
public class TestTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scenarioId;
    private String status;
    private String config;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String resultSummary;
}
