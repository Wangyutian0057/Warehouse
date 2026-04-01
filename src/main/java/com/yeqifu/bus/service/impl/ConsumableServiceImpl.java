package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.Consumable;
import com.yeqifu.bus.mapper.ConsumableMapper;
import com.yeqifu.bus.service.IConsumableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ConsumableServiceImpl extends ServiceImpl<ConsumableMapper, Consumable> implements IConsumableService {

    @Override
    public List<Map<String, Object>> loadConsumableCategoryStat() {
        return this.baseMapper.loadConsumableCategoryStat();
    }
}
