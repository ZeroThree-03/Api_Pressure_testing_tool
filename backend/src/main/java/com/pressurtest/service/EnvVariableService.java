package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.EnvVariableMapper;
import com.pressurtest.model.EnvVariable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvVariableService extends ServiceImpl<EnvVariableMapper, EnvVariable> {

    public List<EnvVariable> listByEnvironment(Long environmentId) {
        return list(new LambdaQueryWrapper<EnvVariable>()
                .eq(EnvVariable::getEnvironmentId, environmentId)
                .orderByAsc(EnvVariable::getId));
    }

    public EnvVariable create(EnvVariable variable) {
        variable.setCreatedAt(LocalDateTime.now());
        getBaseMapper().insert(variable);
        return getOne(new LambdaQueryWrapper<EnvVariable>()
                .orderByDesc(EnvVariable::getId)
                .last("LIMIT 1"));
    }

    public EnvVariable update(Long id, EnvVariable variable) {
        EnvVariable existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("环境变量不存在");
        }
        existing.setName(variable.getName());
        existing.setValue(variable.getValue());
        existing.setDescription(variable.getDescription());
        updateById(existing);
        return getById(id);
    }

    public void delete(Long id) {
        removeById(id);
    }

    public List<EnvVariable> batchCreate(Long environmentId, List<EnvVariable> variables) {
        for (EnvVariable v : variables) {
            v.setEnvironmentId(environmentId);
            v.setCreatedAt(LocalDateTime.now());
        }
        saveBatch(variables);
        return listByEnvironment(environmentId);
    }
}
