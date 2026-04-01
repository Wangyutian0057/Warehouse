package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.AssetRecord;
import java.util.List;
import java.util.Map;

public interface IAssetRecordService extends IService<AssetRecord> {
    IPage<AssetRecord> queryAll(IPage<AssetRecord> page, AssetRecord record, String startTime, String endTime, String direction);

    List<Map<String, Object>> loadAssetTrend(String startTime);
}
