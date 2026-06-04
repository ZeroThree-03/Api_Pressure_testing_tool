package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("global_params")
public class GlobalParam {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String type;
    private String value;
    private String scope;
    private Long scenarioId;
    private LocalDateTime createdAt;
}
