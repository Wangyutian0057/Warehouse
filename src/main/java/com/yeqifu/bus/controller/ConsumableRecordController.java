package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.ConsumableRecord;
import com.yeqifu.bus.service.IConsumableRecordService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("consumableRecord")
public class ConsumableRecordController {

    @Autowired
    private IConsumableRecordService consumableRecordService;

    @RequestMapping("loadAllConsumableRecord")
    @RequiresPermissions("consumable:record:view")
    public DataGridView loadAllConsumableRecord(ConsumableRecord consumableRecord, Integer page, Integer limit, String startTime, String endTime, String direction) {
        Page<ConsumableRecord> pageInfo = new Page<>(page, limit);
        IPage<ConsumableRecord> iPage = consumableRecordService.queryAll(pageInfo, consumableRecord, startTime, endTime, direction);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    @RequestMapping("loadOutportTrend")
    public DataGridView loadOutportTrend(Integer days) {
        if (days == null) days = 30;
        Date startTime = DateUtil.offsetDay(new Date(), -days);
        return new DataGridView(Long.valueOf(0), consumableRecordService.loadTrendStat(DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss")));
    }

    @RequestMapping("exportRecord")
    public void exportRecord(ConsumableRecord consumableRecord, String startTime, String endTime, String direction, HttpServletResponse response) throws IOException {
        Page<ConsumableRecord> pageInfo = new Page<>(1, 10000);
        IPage<ConsumableRecord> iPage = consumableRecordService.queryAll(pageInfo, consumableRecord, startTime, endTime, direction);
        List<ConsumableRecord> list = iPage.getRecords();

        ExcelWriter writer = ExcelUtil.getWriter(false);
        writer.addHeaderAlias("id", "ID");
        writer.addHeaderAlias("consumableType", "耗材名称");
        writer.addHeaderAlias("consumableLocation", "存放位置");
        writer.addHeaderAlias("quantity", "数量");
        writer.addHeaderAlias("type", "操作类型");
        writer.addHeaderAlias("createtime", "操作时间");
        writer.addHeaderAlias("applicant", "申请人/来源");
        writer.addHeaderAlias("operator", "操作人");
        writer.addHeaderAlias("remark", "备注");

        writer.write(list, true);

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        String fileName = "耗材出入库记录.xls";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
    
    @RequestMapping("loadTodayConsumableOutportCount")
    public ResultObj loadTodayConsumableOutportCount() {
        String today = DateUtil.today();
        QueryWrapper<ConsumableRecord> qw = new QueryWrapper<>();
        qw.apply("date_time >= {0}", today + " 00:00:00");
        qw.and(wrapper -> wrapper.like("type", "出库").or().eq("type", "OUT"));
        qw.select("sum(quantity) as quantity");
        Map<String, Object> map = consumableRecordService.getMap(qw);
        if (map == null || map.get("quantity") == null) {
            return new ResultObj(200, "0");
        }
        return new ResultObj(200, String.valueOf(map.get("quantity")));
    }
}
