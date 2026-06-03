package com.pressurtest.controller.dto;

import com.pressurtest.model.ScenarioStep;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioRequest {
    @NotBlank(message = "场景名称不能为空")
    private String name;

    @NotBlank(message = "场景类型不能为空")
    private String type;

    private String description;
    private String config;
    private List<ScenarioStep> steps;
}
