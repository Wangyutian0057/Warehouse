package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.CheckAsset;
import com.yeqifu.bus.mapper.CheckAssetMapper;
import com.yeqifu.bus.service.ICheckAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CheckAssetServiceImpl extends ServiceImpl<CheckAssetMapper, CheckAsset> implements ICheckAssetService {
    
    @Autowired
    private CheckAssetMapper checkAssetMapper;

    @Override
    public List<CheckAsset> queryCheckAssets(Integer checkId) {
        return checkAssetMapper.queryCheckAssets(checkId);
    }
}
