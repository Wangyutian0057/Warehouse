package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.AssetType;
import com.yeqifu.bus.mapper.AssetTypeMapper;
import com.yeqifu.bus.service.IAssetTypeService;
import org.springframework.stereotype.Service;

@Service
public class AssetTypeServiceImpl extends ServiceImpl<AssetTypeMapper, AssetType> implements IAssetTypeService {
}
