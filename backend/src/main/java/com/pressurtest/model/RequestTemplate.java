package com.pressurtest.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("request_templates")
public class RequestTemplate {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String method;
    private String url;
    private String headers;
    private String body;
    private String authType;
    private String authConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
