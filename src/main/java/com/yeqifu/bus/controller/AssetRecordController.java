package com.yeqifu.bus.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.AssetRecord;
import com.yeqifu.bus.service.IAssetRecordService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import cn.hutool.core.date.DateUtil;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("assetRecord")
public class AssetRecordController {

    @Autowired
    private IAssetRecordService assetRecordService;

    @RequestMapping("loadAllAssetRecord")
    @RequiresPermissions("asset:record:view")
    public DataGridView loadAllAssetRecord(AssetRecord assetRecord, Integer page, Integer limit, String startTime, String endTime, String direction) {
        Page<AssetRecord> pageInfo = new Page<>(page, limit);
        // Ensure "盘点平账" is visible if no specific filter is applied or if it matches criteria
        // The service layer handles filtering. We just need to make sure we don't accidentally exclude it.
        // Current service logic usually filters by LIKE 'type'.
        // If direction is provided (IN/OUT), we might need to handle '盘点平账' which is neither strictly IN nor OUT in some contexts, or could be both.
        // But usually user selects 'IN' or 'OUT'. '盘点平账' records might have type="盘点平账".
        // Let's assume the user can search by type="盘点平账" if they want, or view all.
        // No change needed here unless the service explicitly filters OUT '盘点平账'.
        
        IPage<AssetRecord> iPage = assetRecordService.queryAll(pageInfo, assetRecord, startTime, endTime, direction);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    @RequestMapping("exportRecord")
    public void exportRecord(AssetRecord assetRecord, String startTime, String endTime, String direction, HttpServletResponse response) throws IOException {
        Page<AssetRecord> pageInfo = new Page<>(1, 10000); // Export limit
        IPage<AssetRecord> iPage = assetRecordService.queryAll(pageInfo, assetRecord, startTime, endTime, direction);
        List<AssetRecord> list = iPage.getRecords();

        ExcelWriter writer = ExcelUtil.getWriter(false);
        writer.addHeaderAlias("id", "ID");
        writer.addHeaderAlias("maintenanceId", "运维编号");
        writer.addHeaderAlias("assetNo", "资产编号");
        writer.addHeaderAlias("assetType", "设备类型");
        writer.addHeaderAlias("assetBrand", "品牌");
        writer.addHeaderAlias("type", "操作类型");
        writer.addHeaderAlias("createtime", "操作时间");
        writer.addHeaderAlias("applicant", "申请人/来源");
        writer.addHeaderAlias("operator", "操作人");
        writer.addHeaderAlias("remark", "备注");

        writer.write(list, true);

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = "资产出入库记录.xls";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    @RequestMapping("loadAssetTrend")
    public DataGridView loadAssetTrend(Integer days) {
        if (days == null) days = 30;
        Date startTime = DateUtil.offsetDay(new Date(), -days);
        return new DataGridView(Long.valueOf(0), assetRecordService.loadAssetTrend(DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss")));
    }
    
    @RequestMapping("loadTodayAssetOutportCount")
    public ResultObj loadTodayAssetOutportCount() {
        String today = DateUtil.today();
        QueryWrapper<AssetRecord> qw = new QueryWrapper<>();
        qw.apply("date_time >= {0}", today + " 00:00:00");
        qw.and(wrapper -> wrapper.like("type", "出库").or().eq("type", "OUT"));
        return new ResultObj(200, String.valueOf(assetRecordService.count(qw)));
    }
}
