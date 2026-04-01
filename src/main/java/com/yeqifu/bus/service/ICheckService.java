package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.Check;

public interface ICheckService extends IService<Check> {
    /**
     * Start a new inventory check
     */
    void startCheck(Check check);

    /**
     * Finish inventory check and correct stock
     */
    void finishCheck(Integer checkId);

    /**
     * Archive history records before this check
     * @return The archived_at timestamp used
     */
    String archiveHistory(Integer checkId);

    /**
     * Get count of assets with status mismatch
     */
    Integer getAssetMismatchCount(Integer checkId);

    /**
     * Get archived asset records by archive time
     */
    java.util.List<java.util.Map<String, Object>> getArchivedAssetRecords(String archivedTime);

    /**
     * Get archived consumable records by archive time
     */
    java.util.List<java.util.Map<String, Object>> getArchivedConsumableRecords(String archivedTime);
}
