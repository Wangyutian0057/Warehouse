package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.ConsumableRecord;
import java.util.List;
import java.util.Map;

public interface IConsumableRecordService extends IService<ConsumableRecord> {
    IPage<ConsumableRecord> queryAll(IPage<ConsumableRecord> page, ConsumableRecord record, String startTime, String endTime, String direction);
    
    List<Map<String, Object>> loadOutportTrend(String startTime);

    List<Map<String, Object>> loadTrendStat(String startTime);
}
