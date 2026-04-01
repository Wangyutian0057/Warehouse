package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.ConsumableRecord;
import com.yeqifu.bus.mapper.ConsumableRecordMapper;
import com.yeqifu.bus.service.IConsumableRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ConsumableRecordServiceImpl extends ServiceImpl<ConsumableRecordMapper, ConsumableRecord> implements IConsumableRecordService {

    @Override
    public IPage<ConsumableRecord> queryAll(IPage<ConsumableRecord> page, ConsumableRecord record, String startTime, String endTime, String direction) {
        return this.baseMapper.queryAll(page, record, startTime, endTime, direction);
    }

    @Override
    public List<Map<String, Object>> loadOutportTrend(String startTime) {
        return this.baseMapper.loadOutportTrend(startTime);
    }

    @Override
    public List<Map<String, Object>> loadTrendStat(String startTime) {
        return this.baseMapper.loadTrendStat(startTime);
    }
}
