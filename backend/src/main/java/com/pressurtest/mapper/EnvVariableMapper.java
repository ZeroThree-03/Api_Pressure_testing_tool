package com.pressurtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pressurtest.model.EnvVariable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnvVariableMapper extends BaseMapper<EnvVariable> {
}
