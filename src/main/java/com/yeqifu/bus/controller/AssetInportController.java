package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yeqifu.bus.entity.Asset;
import com.yeqifu.bus.entity.AssetRecord;
import com.yeqifu.bus.service.IAssetRecordService;
import com.yeqifu.bus.service.IAssetService;
import com.yeqifu.sys.common.ResultObj;
import com.yeqifu.sys.common.WebUtils;
import com.yeqifu.sys.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("assetInport")
public class AssetInportController {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private IAssetRecordService assetRecordService;

    @RequestMapping("addAssetInport")
    @RequiresPermissions("asset:inport")
    public ResultObj addAssetInport(Asset asset) {
        try {
            User user = (User) WebUtils.getSession().getAttribute("user");
            String operator = (user != null) ? user.getName() : "System";

            // 1. 运维编号处理
            if (StringUtils.isBlank(asset.getMaintenanceId())) {
                String prefix = DateUtil.format(new Date(), "yyMMdd");
                String newId;
                while (true) {
                    newId = prefix + "-" + RandomUtil.randomNumbers(4);
                    int count = assetService.count(new QueryWrapper<Asset>().eq("maintenance_id", newId));
                    if (count == 0) break;
                }
                asset.setMaintenanceId(newId);
            } else {
                int count = assetService.count(new QueryWrapper<Asset>().eq("maintenance_id", asset.getMaintenanceId()));
                if (count > 0) {
                    return new ResultObj(-1, "运维编号已存在");
                }
            }

            if (user != null) {
                asset.setOperator(user.getName());
            }
            asset.setCreateTime(DateUtil.now());
            if (asset.getEntryDate() == null) {
                asset.setEntryDate(DateUtil.now());
            }
            asset.setStatus("IN_STOCK");
            assetService.save(asset);

            // Add Record
            AssetRecord record = new AssetRecord();
            record.setAssetId(asset.getId());
            record.setType("采购入库");
            record.setCreatetime(DateUtil.now());
            record.setOperator(operator);
            record.setRemark("初始入库");
            record.setApplicant(asset.getSource()); 
            assetRecordService.save(record);

            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }

    @RequestMapping("batchImport")
    @RequiresPermissions("asset:inport")
    public ResultObj batchImport(MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.addHeaderAlias("运维编号(留空自动生成)", "maintenanceId");
            reader.addHeaderAlias("设备类型(必填)", "type");
            reader.addHeaderAlias("品牌", "brand");
            reader.addHeaderAlias("序列号", "serialNo");
            reader.addHeaderAlias("资产编号", "assetNo");
            reader.addHeaderAlias("位置", "location");
            reader.addHeaderAlias("来源", "source");
            
            List<Asset> list = reader.readAll(Asset.class);
            
            int success = 0;
            int fail = 0;
            User user = (User) WebUtils.getSession().getAttribute("user");
            String operator = (user != null) ? user.getName() : "System";

            for (Asset asset : list) {
                try {
                    // 基本校验
                    if(StringUtils.isBlank(asset.getType())) {
                        fail++; continue;
                    }
                    
                    // 生成ID
                    if (StringUtils.isBlank(asset.getMaintenanceId())) {
                        String prefix = DateUtil.format(new Date(), "yyMMdd");
                        String newId;
                        while (true) {
                            newId = prefix + "-" + RandomUtil.randomNumbers(4);
                            int count = assetService.count(new QueryWrapper<Asset>().eq("maintenance_id", newId));
                            if (count == 0) break;
                        }
                        asset.setMaintenanceId(newId);
                    } else {
                        if (assetService.count(new QueryWrapper<Asset>().eq("maintenance_id", asset.getMaintenanceId())) > 0) {
                            fail++; continue;
                        }
                    }

                    asset.setStatus("IN_STOCK");
                    asset.setCreateTime(DateUtil.now());
                    if(asset.getEntryDate() == null) asset.setEntryDate(DateUtil.now());
                    asset.setOperator(operator);
                    assetService.save(asset);
                    
                    // Record
                    AssetRecord record = new AssetRecord();
                    record.setAssetId(asset.getId());
                    record.setType("批量入库");
                    record.setCreatetime(DateUtil.now());
                    record.setOperator(operator);
                    record.setRemark("Excel批量导入");
                    record.setApplicant(asset.getSource());
                    assetRecordService.save(record);

                    success++;
                } catch (Exception e) {
                    fail++;
                }
            }
            return new ResultObj(200, "导入完成。成功：" + success + "，失败：" + fail);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ERROR;
        }
    }

    @RequestMapping("downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // false represents xls format (Excel 97-2003)
        ExcelWriter writer = ExcelUtil.getWriter(false);
        writer.addHeaderAlias("maintenanceId", "运维编号(留空自动生成)");
        writer.addHeaderAlias("type", "设备类型(必填)");
        writer.addHeaderAlias("brand", "品牌");
        writer.addHeaderAlias("serialNo", "序列号");
        writer.addHeaderAlias("assetNo", "资产编号");
        writer.addHeaderAlias("location", "位置");
        writer.addHeaderAlias("source", "来源");
        writer.setOnlyAlias(true);
        
        List<Asset> rows = new ArrayList<>();
        Asset example = new Asset();
        example.setMaintenanceId("");
        example.setType("台式电脑");
        example.setBrand("联想");
        example.setSerialNo("SN123456");
        example.setAssetNo("ZC001");
        example.setLocation("一号仓库");
        example.setSource("采购入库");
        rows.add(example);
        
        writer.write(rows, true);

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("资产批量入库模板.xls", "UTF-8"));
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
