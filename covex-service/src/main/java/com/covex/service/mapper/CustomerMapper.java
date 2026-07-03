package com.covex.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.covex.service.entity.CustomerEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {
}
