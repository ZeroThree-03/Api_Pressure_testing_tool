package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.TestTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestTaskMapper extends BaseMapper<TestTask> {
}
