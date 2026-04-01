package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.Consumable;
import java.util.List;
import java.util.Map;

public interface ConsumableMapper extends BaseMapper<Consumable> {
    List<Map<String, Object>> loadConsumableCategoryStat();
}
