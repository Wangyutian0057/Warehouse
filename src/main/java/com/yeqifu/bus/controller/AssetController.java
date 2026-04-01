package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.Asset;
import com.yeqifu.bus.service.IAssetService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.Date;

@RestController
@RequestMapping("asset")
public class AssetController {

    @Autowired
    private IAssetService assetService;

    @RequestMapping("loadAllAsset")
    @RequiresPermissions("asset:view")
    public DataGridView loadAllAsset(Asset asset, Integer page, Integer limit, String startTime, String endTime) {
        Page<Asset> pageInfo = new Page<>(page, limit);
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(asset.getMaintenanceId())) {
            queryWrapper.like("maintenance_id", asset.getMaintenanceId());
        }
        if (StringUtils.isNotBlank(asset.getAssetNo())) {
            queryWrapper.like("asset_no", asset.getAssetNo());
        }
        if (StringUtils.isNotBlank(asset.getType())) {
            queryWrapper.eq("type", asset.getType());
        }
        if (StringUtils.isNotBlank(startTime)) {
            queryWrapper.ge("entry_date", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le("entry_date", endTime);
        }
        queryWrapper.orderByDesc("entry_date");
        
        IPage<Asset> iPage = assetService.page(pageInfo, queryWrapper);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    @RequestMapping("addAsset")
    @RequiresPermissions("asset:create")
    public ResultObj addAsset(Asset asset) {
        try {
            asset.setCreateTime(DateUtil.now());
            assetService.save(asset);
            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }

    @RequestMapping("updateAsset")
    @RequiresPermissions("asset:update")
    public ResultObj updateAsset(Asset asset) {
        try {
            assetService.updateById(asset);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }

    @RequestMapping("deleteAsset")
    @RequiresPermissions("asset:delete")
    public ResultObj deleteAsset(Integer id) {
        try {
            assetService.removeById(id);
            return ResultObj.DELETE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }

    @RequestMapping("batchDeleteAsset")
    @RequiresPermissions("asset:delete")
    public ResultObj batchDeleteAsset(Integer[] ids) {
        try {
            if (ids != null && ids.length > 0) {
                assetService.removeByIds(Arrays.asList(ids));
                return ResultObj.DELETE_SUCCESS;
            } else {
                return ResultObj.DELETE_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }

    @RequestMapping("loadAssetStatusStat")
    public DataGridView loadAssetStatusStat() {
        return new DataGridView(Long.valueOf(0), assetService.loadAssetStatusStat());
    }
    
    @RequestMapping("loadAssetTypeStat")
    public DataGridView loadAssetTypeStat() {
        return new DataGridView(Long.valueOf(0), assetService.loadAssetTypeStat());
    }
    
    @RequestMapping("loadInStockAssetTypeStat")
    public DataGridView loadInStockAssetTypeStat() {
        return new DataGridView(Long.valueOf(0), assetService.loadInStockAssetTypeStat());
    }
    
    @RequestMapping("loadOutStockAssetTypeStat")
    public DataGridView loadOutStockAssetTypeStat() {
        return new DataGridView(Long.valueOf(0), assetService.loadOutStockAssetTypeStat());
    }
    
    @RequestMapping("loadAssetCount")
    public ResultObj loadAssetCount() {
        return new ResultObj(200, String.valueOf(assetService.count()));
    }
}
