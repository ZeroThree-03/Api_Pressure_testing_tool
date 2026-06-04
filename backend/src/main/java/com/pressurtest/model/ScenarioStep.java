package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenario_steps")
public class ScenarioStep {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long scenarioId;
    private Long templateId;
    private Integer stepOrder;
    private String name;
    private String config;
}
