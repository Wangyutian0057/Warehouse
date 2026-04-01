package com.yeqifu.bus.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.*;
import com.yeqifu.bus.mapper.AssetRecordArchiveMapper;
import com.yeqifu.bus.mapper.CheckMapper;
import com.yeqifu.bus.mapper.ConsumableRecordArchiveMapper;
import com.yeqifu.bus.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CheckServiceImpl extends ServiceImpl<CheckMapper, Check> implements ICheckService {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private IConsumableService consumableService;
    @Autowired
    private ICheckAssetService checkAssetService;
    @Autowired
    private ICheckConsumableService checkConsumableService;
    @Autowired
    private IAssetRecordService assetRecordService;
    @Autowired
    private IConsumableRecordService consumableRecordService;
    @Autowired
    private AssetRecordArchiveMapper assetRecordArchiveMapper;
    @Autowired
    private ConsumableRecordArchiveMapper consumableRecordArchiveMapper;

    @Override
    public void startCheck(Check check) {
        check.setCreateTime(DateUtil.now());
        check.setStatus(0); // Draft
        this.save(check);

        // Snapshot Assets
        List<Asset> assets = assetService.list();
        List<CheckAsset> checkAssets = new ArrayList<>();
        for (Asset a : assets) {
            CheckAsset ca = new CheckAsset();
            ca.setCheckId(check.getId());
            ca.setAssetId(a.getId());
            ca.setExpectedStatus(a.getStatus());
            ca.setActualStatus(a.getStatus());
            ca.setExpectedLocation(a.getLocation());
            ca.setActualLocation(a.getLocation());
            ca.setResult("MATCH");
            checkAssets.add(ca);
        }
        if (!checkAssets.isEmpty()) {
            checkAssetService.saveBatch(checkAssets);
        }

        // Snapshot Consumables
        List<Consumable> consumables = consumableService.list();
        List<CheckConsumable> checkConsumables = new ArrayList<>();
        for (Consumable c : consumables) {
            CheckConsumable cc = new CheckConsumable();
            cc.setCheckId(check.getId());
            cc.setConsumableId(c.getId());
            cc.setExpectedQuantity(c.getQuantity());
            cc.setActualQuantity(c.getQuantity());
            cc.setDiffQuantity(0);
            checkConsumables.add(cc);
        }
        if (!checkConsumables.isEmpty()) {
            checkConsumableService.saveBatch(checkConsumables);
        }
    }

    @Override
    public void finishCheck(Integer checkId) {
        Check check = this.getById(checkId);
        if (check == null || check.getStatus() == 1) {
            return;
        }

        // 1. Update Consumable Inventory
        List<CheckConsumable> consumableDetails = checkConsumableService.list(new QueryWrapper<CheckConsumable>().eq("check_id", checkId));
        for (CheckConsumable cc : consumableDetails) {
            if (cc.getActualQuantity() != null && !cc.getActualQuantity().equals(cc.getExpectedQuantity())) {
                Consumable c = consumableService.getById(cc.getConsumableId());
                if (c != null) {
                    c.setQuantity(cc.getActualQuantity());
                    consumableService.updateById(c);
                    
                    // Create balancing record in ACTIVE table
                    ConsumableRecord record = new ConsumableRecord();
                    record.setConsumableId(c.getId());
                    // Determine type based on diff
                    if (cc.getDiffQuantity() > 0) {
                        record.setType("盘点入库");
                    } else {
                        record.setType("盘点出库");
                    }
                    record.setQuantity(Math.abs(cc.getDiffQuantity())); 
                    record.setCreatetime(DateUtil.now());
                    record.setOperator(check.getOperName());
                    record.setRemark("盘点ID: " + checkId + ", 修正: " + cc.getExpectedQuantity() + "->" + cc.getActualQuantity());
                    consumableRecordService.save(record);
                }
            }
        }
        
        // 2. Update Asset Status and Location
        List<CheckAsset> assetDetails = checkAssetService.list(new QueryWrapper<CheckAsset>().eq("check_id", checkId));
        for (CheckAsset ca : assetDetails) {
            boolean statusChanged = ca.getActualStatus() != null && !ca.getActualStatus().equals(ca.getExpectedStatus());
            boolean locationChanged = ca.getActualLocation() != null && !ca.getActualLocation().equals(ca.getExpectedLocation());
            
            if (statusChanged || locationChanged) {
                Asset a = assetService.getById(ca.getAssetId());
                if (a != null) {
                    StringBuilder remark = new StringBuilder("盘点修正: ");
                    String recordType = "盘点修正"; // Default
                    
                    if (statusChanged) {
                        a.setStatus(ca.getActualStatus());
                        remark.append("状态(").append(ca.getExpectedStatus()).append("->").append(ca.getActualStatus()).append(") ");
                        
                        // Determine In/Out based on status
                        if ("IN_STOCK".equals(ca.getActualStatus())) {
                            recordType = "盘点入库";
                        } else if ("IN_STOCK".equals(ca.getExpectedStatus())) {
                            recordType = "盘点出库";
                        }
                    }
                    if (locationChanged) {
                        a.setLocation(ca.getActualLocation());
                        remark.append("位置(").append(ca.getExpectedLocation()).append("->").append(ca.getActualLocation()).append(") ");
                    }
                    
                    assetService.updateById(a);
                    
                    // Create balancing record in ACTIVE table
                    AssetRecord record = new AssetRecord();
                    record.setAssetId(a.getId());
                    record.setType(recordType);
                    record.setCreatetime(DateUtil.now());
                    record.setOperator(check.getOperName());
                    record.setRemark(remark.toString() + ", 盘点ID: " + checkId);
                    assetRecordService.save(record);
                }
            }
        }

        check.setStatus(1); // Finished
        check.setFinishTime(DateUtil.now());
        this.updateById(check);
    }

    @Override
    public Integer getAssetMismatchCount(Integer checkId) {
        int count = 0;
        
        // 1. Check Assets
        QueryWrapper<CheckAsset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("check_id", checkId);
        List<CheckAsset> list = checkAssetService.list(queryWrapper);
        for (CheckAsset ca : list) {
            String expectedStatus = ca.getExpectedStatus() == null ? "" : ca.getExpectedStatus();
            String actualStatus = ca.getActualStatus() == null ? "" : ca.getActualStatus();
            String expectedLocation = ca.getExpectedLocation() == null ? "" : ca.getExpectedLocation();
            String actualLocation = ca.getActualLocation() == null ? "" : ca.getActualLocation();
            
            boolean statusMismatch = !expectedStatus.equals(actualStatus);
            boolean locationMismatch = !expectedLocation.equals(actualLocation);
            
            if (statusMismatch || locationMismatch) {
                count++;
            }
        }
        
        // 2. Check Consumables
        QueryWrapper<CheckConsumable> consWrapper = new QueryWrapper<>();
        consWrapper.eq("check_id", checkId);
        List<CheckConsumable> consList = checkConsumableService.list(consWrapper);
        for (CheckConsumable cc : consList) {
            if (cc.getDiffQuantity() != null && cc.getDiffQuantity() != 0) {
                count++;
            }
        }
        
        return count;
    }

    @Override
    public String archiveHistory(Integer checkId) {
        Check check = this.getById(checkId);
        if (check == null || check.getStatus() != 1) {
            throw new RuntimeException("Check must be finished before archiving.");
        }
        // Archive all records up to check finish time
        String archiveTime = check.getFinishTime();
        if (archiveTime == null) {
            archiveTime = DateUtil.now();
        }

        // Archive Asset Records
        // Select records created before or at archiveTime
        QueryWrapper<AssetRecord> assetWrapper = new QueryWrapper<>();
        assetWrapper.le("date_time", archiveTime);
        
        List<AssetRecord> assetRecords = assetRecordService.list(assetWrapper);
        if (assetRecords != null && !assetRecords.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (AssetRecord r : assetRecords) {
                AssetRecordArchive archive = new AssetRecordArchive();
                BeanUtil.copyProperties(r, archive);
                archive.setArchivedAt(archiveTime);
                assetRecordArchiveMapper.insert(archive);
                ids.add(r.getId());
            }
            // Delete original records
            if (!ids.isEmpty()) {
                assetRecordService.removeByIds(ids);
            }
        }

        // Archive Consumable Records
        QueryWrapper<ConsumableRecord> consumableWrapper = new QueryWrapper<>();
        consumableWrapper.le("date_time", archiveTime);
        
        List<ConsumableRecord> consumableRecords = consumableRecordService.list(consumableWrapper);
        if (consumableRecords != null && !consumableRecords.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            for (ConsumableRecord r : consumableRecords) {
                ConsumableRecordArchive archive = new ConsumableRecordArchive();
                BeanUtil.copyProperties(r, archive);
                archive.setArchivedAt(archiveTime);
                consumableRecordArchiveMapper.insert(archive);
                ids.add(r.getId());
            }
            // Delete original records
            if (!ids.isEmpty()) {
                consumableRecordService.removeByIds(ids);
            }
        }
        return archiveTime;
    }

    @Override
    public List<java.util.Map<String, Object>> getArchivedAssetRecords(String archivedTime) {
        // Query bus_asset_record_archive where archived_at = archivedTime
        // We need a custom mapper method or use wrapper with Maps
        // Since archive table might not have an Entity in MP completely mapped or we want specific cols.
        // Let's use the mapper directly if possible or QueryWrapper on Archive Entity.
        // But AssetRecordArchive is likely an entity.
        // Let's use QueryWrapper<AssetRecordArchive>
        QueryWrapper<AssetRecordArchive> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("archived_at", archivedTime);
        // We need to join with Asset to get Asset Details? 
        // The Archive record usually only has Asset ID? 
        // Yes, AssetRecordArchive usually copies AssetRecord which has AssetId.
        // So we need to join bus_asset to get name/type etc.
        // But AssetRecordArchiveMapper might not have join logic pre-written.
        // Let's check AssetRecordArchiveMapper.
        // If not, we can fetch list and populate names manually or write a custom XML query.
        // Writing custom XML query is cleaner.
        return assetRecordArchiveMapper.queryArchivedRecordsWithDetails(archivedTime);
    }

    @Override
    public List<java.util.Map<String, Object>> getArchivedConsumableRecords(String archivedTime) {
        return consumableRecordArchiveMapper.queryArchivedRecordsWithDetails(archivedTime);
    }
}
