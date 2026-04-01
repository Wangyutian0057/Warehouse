package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.AssetRecordArchive;

import org.apache.ibatis.annotations.Param;

public interface AssetRecordArchiveMapper extends BaseMapper<AssetRecordArchive> {
    java.util.List<java.util.Map<String, Object>> queryArchivedRecordsWithDetails(@Param("archivedTime") String archivedTime);
}
