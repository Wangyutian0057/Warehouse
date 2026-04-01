package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.AssetRecord;
import com.yeqifu.bus.mapper.AssetRecordMapper;
import com.yeqifu.bus.service.IAssetRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AssetRecordServiceImpl extends ServiceImpl<AssetRecordMapper, AssetRecord> implements IAssetRecordService {

    @Override
    public IPage<AssetRecord> queryAll(IPage<AssetRecord> page, AssetRecord record, String startTime, String endTime, String direction) {
        return this.baseMapper.queryAll(page, record, startTime, endTime, direction);
    }

    @Override
    public List<Map<String, Object>> loadAssetTrend(String startTime) {
        return this.baseMapper.loadAssetTrend(startTime);
    }
}
