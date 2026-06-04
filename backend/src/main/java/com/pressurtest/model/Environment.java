package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("environments")
public class Environment {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String baseUrl;
    private String description;
    private Integer isActive;
    private LocalDateTime createdAt;
}
