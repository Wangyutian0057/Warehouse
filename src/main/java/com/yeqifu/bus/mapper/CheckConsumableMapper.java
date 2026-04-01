package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.CheckConsumable;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CheckConsumableMapper extends BaseMapper<CheckConsumable> {
    List<CheckConsumable> queryCheckConsumables(@Param("checkId") Integer checkId);
}
