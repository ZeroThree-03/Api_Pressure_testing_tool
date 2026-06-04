package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("env_variables")
public class EnvVariable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long environmentId;
    private String name;
    private String value;
    private String description;
    private LocalDateTime createdAt;
}
