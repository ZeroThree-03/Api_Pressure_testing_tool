package com.pressurtest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pressurtest.mapper.ScenarioStepMapper;
import com.pressurtest.mapper.TestScenarioMapper;
import com.pressurtest.model.ScenarioStep;
import com.pressurtest.model.TestScenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final TestScenarioMapper scenarioMapper;
    private final ScenarioStepMapper stepMapper;

    public List<TestScenario> listAll() {
        return scenarioMapper.selectList(
                new LambdaQueryWrapper<TestScenario>()
                        .orderByDesc(TestScenario::getUpdatedAt));
    }

    public TestScenario getById(Long id) {
        return scenarioMapper.selectById(id);
    }

    public List<ScenarioStep> getSteps(Long scenarioId) {
        return stepMapper.selectList(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, scenarioId)
                        .orderByAsc(ScenarioStep::getStepOrder));
    }

    @Transactional
    public TestScenario create(TestScenario scenario, List<ScenarioStep> steps) {
        scenario.setCreatedAt(LocalDateTime.now());
        scenario.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.insert(scenario);
        // SQLite doesn't support getGeneratedKeys, so fetch the latest entry
        TestScenario saved = scenarioMapper.selectOne(
                new LambdaQueryWrapper<TestScenario>()
                        .orderByDesc(TestScenario::getId)
                        .last("LIMIT 1"));
        saveSteps(saved.getId(), steps);
        return saved;
    }

    @Transactional
    public TestScenario update(Long id, TestScenario scenario, List<ScenarioStep> steps) {
        TestScenario existing = scenarioMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("场景不存在");
        }

        scenario.setId(id);
        scenario.setCreatedAt(existing.getCreatedAt());
        scenario.setUpdatedAt(LocalDateTime.now());
        scenarioMapper.updateById(scenario);

        stepMapper.delete(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, id));
        saveSteps(id, steps);

        return scenario;
    }

    private void saveSteps(Long scenarioId, List<ScenarioStep> steps) {
        if (steps == null) return;
        for (int i = 0; i < steps.size(); i++) {
            ScenarioStep step = steps.get(i);
            step.setScenarioId(scenarioId);
            step.setStepOrder(i + 1);
            stepMapper.insert(step);
        }
    }

    @Transactional
    public void delete(Long id) {
        stepMapper.delete(
                new LambdaQueryWrapper<ScenarioStep>()
                        .eq(ScenarioStep::getScenarioId, id));
        scenarioMapper.deleteById(id);
    }
}
