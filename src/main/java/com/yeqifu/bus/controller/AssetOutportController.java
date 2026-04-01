package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import com.yeqifu.bus.entity.Asset;
import com.yeqifu.bus.entity.AssetRecord;
import com.yeqifu.bus.service.IAssetRecordService;
import com.yeqifu.bus.service.IAssetService;
import com.yeqifu.sys.common.ResultObj;
import com.yeqifu.sys.common.WebUtils;
import com.yeqifu.sys.entity.User;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;

@RestController
@RequestMapping("assetOutport")
public class AssetOutportController {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private IAssetRecordService assetRecordService;

    @RequestMapping("addAssetOutport")
    @RequiresPermissions("asset:outport")
    public ResultObj addAssetOutport(Integer assetId, String applicant, String remark, String type) {
        try {
            User user = (User) WebUtils.getSession().getAttribute("user");
            
            Asset asset = assetService.getById(assetId);
            if (asset == null) return ResultObj.ERROR;
            
            // Check if already outported
            if ("OUT".equals(asset.getStatus())) {
                return new ResultObj(-1, "该资产已出库，无法再次出库");
            }
            
            asset.setStatus("OUT");
            assetService.updateById(asset);

            // Record
            AssetRecord record = new AssetRecord();
            record.setAssetId(assetId);
            record.setType(type != null ? type : "OUT");
            record.setCreatetime(DateUtil.now());
            if (user != null) {
                record.setOperator(user.getName());
            }
            record.setApplicant(applicant);
            record.setRemark(remark);
            assetRecordService.save(record);

            return ResultObj.OPERATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.OPERATE_ERROR;
        }
    }

    @RequestMapping("deleteOutport")
    @RequiresPermissions("asset:outport")
    public ResultObj deleteOutport(Integer id) {
        try {
            AssetRecord record = assetRecordService.getById(id);
            if (record != null) {
                // Revert Asset Status
                Asset asset = assetService.getById(record.getAssetId());
                if (asset != null) {
                    asset.setStatus("IN_STOCK");
                    assetService.updateById(asset);
                }
                assetRecordService.removeById(id);
                return ResultObj.DELETE_SUCCESS;
            }
            return ResultObj.DELETE_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }

    @RequestMapping("updateOutport")
    public ResultObj updateOutport(AssetRecord record) {
        try {
            assetRecordService.updateById(record);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }
}
