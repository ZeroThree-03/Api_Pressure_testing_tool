package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_scenarios")
public class TestScenario {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String description;
    private String config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
