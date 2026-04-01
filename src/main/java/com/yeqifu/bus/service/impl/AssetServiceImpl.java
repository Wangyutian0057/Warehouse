package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.Asset;
import com.yeqifu.bus.mapper.AssetMapper;
import com.yeqifu.bus.service.IAssetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements IAssetService {
    @Override
    public List<Map<String, Object>> loadAssetStatusStat() {
        return this.baseMapper.loadAssetStatusStat();
    }

    @Override
    public List<Map<String, Object>> loadAssetTypeStat() {
        return this.baseMapper.loadAssetTypeStat();
    }
    
    @Override
    public List<Map<String, Object>> loadInStockAssetTypeStat() {
        return this.baseMapper.loadInStockAssetTypeStat();
    }
    
    @Override
    public List<Map<String, Object>> loadOutStockAssetTypeStat() {
        return this.baseMapper.loadOutStockAssetTypeStat();
    }
}
