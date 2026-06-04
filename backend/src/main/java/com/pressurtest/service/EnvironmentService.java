package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.EnvironmentMapper;
import com.pressurtest.model.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvironmentService extends ServiceImpl<EnvironmentMapper, Environment> {

    public List<Environment> listAll() {
        return list(new LambdaQueryWrapper<Environment>().orderByAsc(Environment::getId));
    }

    public Environment create(Environment env) {
        env.setCreatedAt(LocalDateTime.now());
        if (env.getIsActive() == null) {
            env.setIsActive(0);
        }
        save(env);
        return getOne(new LambdaQueryWrapper<Environment>()
                .orderByDesc(Environment::getId)
                .last("LIMIT 1"));
    }

    public Environment update(Long id, Environment env) {
        Environment existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("环境不存在");
        }
        existing.setName(env.getName());
        existing.setBaseUrl(env.getBaseUrl());
        existing.setDescription(env.getDescription());
        updateById(existing);
        return getById(id);
    }

    public void delete(Long id) {
        removeById(id);
    }

    public Environment activate(Long id) {
        Environment env = getById(id);
        if (env == null) {
            throw new IllegalArgumentException("环境不存在");
        }
        // 取消所有环境的激活状态
        List<Environment> all = list();
        for (Environment e : all) {
            if (e.getIsActive() != null && e.getIsActive() == 1) {
                e.setIsActive(0);
                updateById(e);
            }
        }
        // 激活目标环境
        env.setIsActive(1);
        updateById(env);
        return env;
    }
}
