package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.Asset;
import java.util.List;
import java.util.Map;

public interface AssetMapper extends BaseMapper<Asset> {
    List<Map<String, Object>> loadAssetStatusStat();
    List<Map<String, Object>> loadAssetTypeStat();
    List<Map<String, Object>> loadInStockAssetTypeStat();
    List<Map<String, Object>> loadOutStockAssetTypeStat();
}
