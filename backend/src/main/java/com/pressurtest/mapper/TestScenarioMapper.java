package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.TestScenario;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestScenarioMapper extends BaseMapper<TestScenario> {
}
