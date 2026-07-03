package com.covex.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.covex.service.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {
}
