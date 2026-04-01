package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.CheckAsset;
import java.util.List;

public interface ICheckAssetService extends IService<CheckAsset> {
    List<CheckAsset> queryCheckAssets(Integer checkId);
}
