package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.RequestTemplateMapper;
import com.pressurtest.model.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService extends ServiceImpl<RequestTemplateMapper, RequestTemplate> {

    public List<RequestTemplate> listAll() {
        return list(new LambdaQueryWrapper<RequestTemplate>()
                .orderByDesc(RequestTemplate::getUpdatedAt));
    }

    public RequestTemplate getById(Long id) {
        return super.getById(id);
    }

    public RequestTemplate create(RequestTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        save(template);
        // SQLite doesn't support getGeneratedKeys, so fetch the latest entry
        return getOne(new LambdaQueryWrapper<RequestTemplate>()
                .orderByDesc(RequestTemplate::getId)
                .last("LIMIT 1"));
    }

    public RequestTemplate update(Long id, RequestTemplate template) {
        RequestTemplate existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        template.setId(id);
        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);
        return template;
    }

    public void delete(Long id) {
        removeById(id);
    }
}
