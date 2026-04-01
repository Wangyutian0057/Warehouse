package com.yeqifu.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeqifu.bus.entity.ConsumableRecordArchive;

import org.apache.ibatis.annotations.Param;

public interface ConsumableRecordArchiveMapper extends BaseMapper<ConsumableRecordArchive> {
    java.util.List<java.util.Map<String, Object>> queryArchivedRecordsWithDetails(@Param("archivedTime") String archivedTime);
}
