package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yeqifu.bus.entity.AssetRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AssetRecordMapper extends BaseMapper<AssetRecord> {
    IPage<AssetRecord> queryAll(@Param("page") IPage<AssetRecord> page, @Param("record") AssetRecord record, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("direction") String direction);

    List<Map<String, Object>> loadAssetTrend(@Param("startTime") String startTime);
}
