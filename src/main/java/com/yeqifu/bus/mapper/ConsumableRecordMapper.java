package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yeqifu.bus.entity.ConsumableRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import java.util.List;
import java.util.Map;

public interface ConsumableRecordMapper extends BaseMapper<ConsumableRecord> {
    IPage<ConsumableRecord> queryAll(@Param("page") IPage<ConsumableRecord> page, @Param("record") ConsumableRecord record, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("direction") String direction);

    List<Map<String, Object>> loadOutportTrend(@Param("startTime") String startTime);
    
    List<Map<String, Object>> loadTrendStat(@Param("startTime") String startTime);
}
