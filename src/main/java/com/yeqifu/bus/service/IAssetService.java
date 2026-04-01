package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.Asset;
import java.util.List;
import java.util.Map;

public interface IAssetService extends IService<Asset> {
    List<Map<String, Object>> loadAssetStatusStat();
    List<Map<String, Object>> loadAssetTypeStat();
    List<Map<String, Object>> loadInStockAssetTypeStat();
    List<Map<String, Object>> loadOutStockAssetTypeStat();
}
