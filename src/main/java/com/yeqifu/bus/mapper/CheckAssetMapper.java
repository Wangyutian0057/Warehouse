package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.CheckAsset;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CheckAssetMapper extends BaseMapper<CheckAsset> {
    List<CheckAsset> queryCheckAssets(@Param("checkId") Integer checkId);
}
