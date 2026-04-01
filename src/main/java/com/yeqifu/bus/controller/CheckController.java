package com.yeqifu.bus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.Check;
import com.yeqifu.bus.entity.CheckAsset;
import com.yeqifu.bus.entity.CheckConsumable;
import com.yeqifu.bus.service.ICheckAssetService;
import com.yeqifu.bus.service.ICheckConsumableService;
import com.yeqifu.bus.service.ICheckService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import com.yeqifu.sys.common.WebUtils;
import com.yeqifu.sys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("check")
public class CheckController {

    @Autowired
    private ICheckService checkService;
    @Autowired
    private ICheckAssetService checkAssetService;
    @Autowired
    private ICheckConsumableService checkConsumableService;

    @RequestMapping("toCheckManager")
    public ModelAndView toCheckManager() {
        return new ModelAndView("business/check/checkManager");
    }

    @RequestMapping("exportArchive")
    public void exportArchive(Integer id, javax.servlet.http.HttpServletResponse response) {
        try {
            Check check = checkService.getById(id);
            // Reusing exportCheck logic as requested, since it contains the balancing info (Diff Quantity / Actual Status)
            this.exportCheck(id, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check for Mismatches
    @RequestMapping("getCheckMismatchCount")
    public ResultObj getCheckMismatchCount(Integer id) {
        try {
            Integer count = checkService.getAssetMismatchCount(id);
            return new ResultObj(200, count.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ERROR;
        }
    }

    // Export Check Result
    @RequestMapping("exportCheck")
    public void exportCheck(Integer id, javax.servlet.http.HttpServletResponse response) {
        try {
            Check check = checkService.getById(id);
            List<CheckAsset> assets = checkAssetService.queryCheckAssets(id);
            List<CheckConsumable> consumables = checkConsumableService.queryCheckConsumables(id);

            cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter();
            
            // Sheet 1: Assets
            writer.setSheet("资产盘点");
            writer.addHeaderAlias("maintenanceId", "运维编号");
            writer.addHeaderAlias("assetType", "设备类型");
            writer.addHeaderAlias("brand", "品牌");
            writer.addHeaderAlias("serialNo", "序列号");
            writer.addHeaderAlias("assetNo", "资产编号");
            writer.addHeaderAlias("location", "位置");
            writer.addHeaderAlias("expectedStatus", "系统状态");
            writer.addHeaderAlias("actualStatus", "实盘状态");
            writer.addHeaderAlias("expectedLocation", "系统位置");
            writer.addHeaderAlias("actualLocation", "实盘位置");
            writer.addHeaderAlias("result", "结果");
            writer.addHeaderAlias("createTime", "入库时间");
            writer.addHeaderAlias("assetOperator", "入库人");
            writer.addHeaderAlias("remark", "备注");
            writer.setOnlyAlias(true);
            writer.write(assets, true);

            // Sheet 2: Consumables
            writer.setSheet("耗材盘点");
            writer.addHeaderAlias("consumableName", "耗材名称");
            writer.addHeaderAlias("location", "位置");
            writer.addHeaderAlias("expectedQuantity", "账面数量");
            writer.addHeaderAlias("actualQuantity", "实盘数量");
            writer.addHeaderAlias("diffQuantity", "差异数量");
            writer.addHeaderAlias("remark", "备注");
            writer.setOnlyAlias(true);
            writer.write(consumables, true);

            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String fileName = java.net.URLEncoder.encode("库存盘点单_" + check.getTitle(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
            
            javax.servlet.ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load Checks
    @RequestMapping("loadAllCheck")
    public DataGridView loadAllCheck(Check check, Integer page, Integer limit) {
        Page<Check> pageInfo = new Page<>(page, limit);
        QueryWrapper<Check> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        IPage<Check> iPage = checkService.page(pageInfo, queryWrapper);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    // Start Check
    @RequestMapping("addCheck")
    public ResultObj addCheck(Check check) {
        try {
            User user = (User) WebUtils.getSession().getAttribute("user");
            if(user != null) {
                check.setOperName(user.getName());
            }
            checkService.startCheck(check);
            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }

    // Finish Check
    @RequestMapping("finishCheck")
    public ResultObj finishCheck(Integer id) {
        try {
            checkService.finishCheck(id);
            return ResultObj.OPERATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.OPERATE_ERROR;
        }
    }
    
    // Archive
    @RequestMapping("archiveCheck")
    public ResultObj archiveCheck(Integer id) {
        try {
            String archiveTime = checkService.archiveHistory(id);
            return new ResultObj(200, archiveTime);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultObj(-1, "Archive Failed");
        }
    }
    
    // Export Archived Records
    @RequestMapping("exportArchivedRecords")
    public void exportArchivedRecords(String time, javax.servlet.http.HttpServletResponse response) {
         try {
             System.out.println("Exporting Archive for time: " + time);
             // 1. Fetch data
             List<java.util.Map<String, Object>> assetRecords = checkService.getArchivedAssetRecords(time);
             List<java.util.Map<String, Object>> consumableRecords = checkService.getArchivedConsumableRecords(time);
             
             System.out.println("Asset Records Count: " + (assetRecords != null ? assetRecords.size() : 0));
             if (assetRecords != null && !assetRecords.isEmpty()) {
                 System.out.println("First Asset Record Keys: " + assetRecords.get(0).keySet());
             }
             System.out.println("Consumable Records Count: " + (consumableRecords != null ? consumableRecords.size() : 0));
             if (consumableRecords != null && !consumableRecords.isEmpty()) {
                 System.out.println("First Consumable Record Keys: " + consumableRecords.get(0).keySet());
             }
             
             // 2. Filter In/Out for separate sheets (if needed) or just group them?
             // Prompt says: "Asset In/Out records" and "Consumable In/Out records".
             // We can make 4 sheets or 2 sheets with Type column.
             // Let's make 2 sheets: "资产出入库记录" and "耗材出入库记录".
             
             cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter();
             
             // Sheet 1: Asset Records
             writer.setSheet("资产出入库记录");
             writer.addHeaderAlias("id", "ID");
             writer.addHeaderAlias("maintenanceId", "运维编号");
             writer.addHeaderAlias("assetType", "设备类型");
             writer.addHeaderAlias("assetBrand", "品牌");
             writer.addHeaderAlias("serialNo", "序列号");
             writer.addHeaderAlias("assetNo", "资产编号");
             writer.addHeaderAlias("type", "操作类型"); // In/Out
             writer.addHeaderAlias("location", "位置");
             writer.addHeaderAlias("applicant", "来源/去向");
             writer.addHeaderAlias("operator", "操作人");
             writer.addHeaderAlias("date_time", "操作时间");
             writer.addHeaderAlias("remark", "备注");
             writer.setOnlyAlias(true);
             writer.write(assetRecords, true);
             
             // Sheet 2: Consumable Records
            writer.setSheet("耗材出入库记录");
            writer.addHeaderAlias("id", "ID");
            writer.addHeaderAlias("consumableName", "耗材名称");
            writer.addHeaderAlias("type", "操作类型");
            writer.addHeaderAlias("quantity", "数量");
            writer.addHeaderAlias("applicant", "领用人/来源");
            writer.addHeaderAlias("operator", "操作人");
            writer.addHeaderAlias("date_time", "操作时间");
            writer.addHeaderAlias("remark", "备注");
            writer.setOnlyAlias(true);
            writer.write(consumableRecords, true);
             
             response.setContentType("application/vnd.ms-excel;charset=utf-8");
             String fileName = java.net.URLEncoder.encode("历史归档记录_" + time.replace(" ", "_").replace(":", "-"), "UTF-8");
             response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
             
             javax.servlet.ServletOutputStream out = response.getOutputStream();
             writer.flush(out, true);
             writer.close();
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
    }

    // Load Check Assets
    @RequestMapping("loadCheckAssets")
    public DataGridView loadCheckAssets(Integer checkId) {
        List<CheckAsset> list = checkAssetService.queryCheckAssets(checkId);
        return new DataGridView((long) list.size(), list);
    }
    
    // Load Check Consumables
    @RequestMapping("loadCheckConsumables")
    public DataGridView loadCheckConsumables(Integer checkId) {
        List<CheckConsumable> list = checkConsumableService.queryCheckConsumables(checkId);
        return new DataGridView((long) list.size(), list);
    }
    
    // Update Check Asset
    @RequestMapping("updateCheckAsset")
    public ResultObj updateCheckAsset(CheckAsset checkAsset) {
        try {
            // Fetch existing to compare
            CheckAsset existing = checkAssetService.getById(checkAsset.getId());
            if (existing != null) {
                if(checkAsset.getActualStatus() != null) existing.setActualStatus(checkAsset.getActualStatus());
                if(checkAsset.getActualLocation() != null) existing.setActualLocation(checkAsset.getActualLocation());
                if(checkAsset.getRemark() != null) existing.setRemark(checkAsset.getRemark());
                
                boolean statusDiff = existing.getActualStatus() != null && !existing.getActualStatus().equals(existing.getExpectedStatus());
                boolean locDiff = existing.getActualLocation() != null && !existing.getActualLocation().equals(existing.getExpectedLocation());
                
                if (statusDiff || locDiff) {
                    existing.setResult("CHANGED");
                } else {
                    existing.setResult("MATCH");
                }
                
                checkAssetService.updateById(existing);
            }
            return ResultObj.UPDATE_SUCCESS; // Returning success even if not found, or could return error
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }
    
    // Update Check Consumable
    @RequestMapping("updateCheckConsumable")
    public ResultObj updateCheckConsumable(CheckConsumable checkConsumable) {
        try {
            if(checkConsumable.getActualQuantity() != null && checkConsumable.getExpectedQuantity() != null) {
                checkConsumable.setDiffQuantity(checkConsumable.getActualQuantity() - checkConsumable.getExpectedQuantity());
            }
            checkConsumableService.updateById(checkConsumable);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }
}
