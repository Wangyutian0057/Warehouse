package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yeqifu.bus.entity.Consumable;
import com.yeqifu.bus.entity.ConsumableRecord;
import com.yeqifu.bus.service.IConsumableRecordService;
import com.yeqifu.bus.service.IConsumableService;
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
@RequestMapping("consumableInport")
public class ConsumableInportController {

    @Autowired
    private IConsumableService consumableService;
    @Autowired
    private IConsumableRecordService consumableRecordService;

    @RequestMapping("addConsumableInport")
    @RequiresPermissions("consumable:inport")
    public ResultObj addConsumableInport(Consumable consumable, Integer inportQuantity, String source) {
        try {
            User user = (User) WebUtils.getSession().getAttribute("user");
            
            // Check if exists
            QueryWrapper<Consumable> qw = new QueryWrapper<>();
            qw.eq("type", consumable.getType());
            Consumable exist = consumableService.getOne(qw);
            
            if (exist != null) {
                // If exists, do not allow adding new with same name (per requirement), or handle as adding stock?
                // Requirement: "if same name exists, prompt already recorded, cannot add new" -> This implies creating new entry logic.
                // But for "Inport", usually it means adding stock. 
                // However, the prompt says "无法新增", likely meaning "Cannot create a new Consumable record if type exists".
                // BUT, this is an INPORT action. 
                // If the user intends to ADD STOCK to existing item, they should use a specific "Inport" button on the row, OR the system should auto-merge.
                // The requirement "如果该耗材已经有同名记录则提示已经记录过该物资，无法新增" strongly suggests this is about CREATING a new consumable type.
                // But the function is "addConsumableInport".
                
                // Let's look at the UI. There is "add" button which calls this.
                // If the user wants to add stock to existing item, the new requirement says: "在耗材列表的操作部分新增入库按钮".
                // So, the top "add" button is for NEW consumable types.
                return new ResultObj(-1, "该耗材已存在，请在列表操作栏点击【入库】按钮进行库存添加！");
            } else {
                consumable.setQuantity(inportQuantity);
                consumable.setUpdateTime(DateUtil.now());
                consumable.setInsertTime(DateUtil.now());
                consumableService.save(consumable);
            }

            // Record
            ConsumableRecord record = new ConsumableRecord();
            record.setConsumableId(consumable.getId());
            record.setType("采购入库"); // Default or from source? The UI has "source" select.
            if (StringUtils.isNotBlank(source)) {
                 record.setType(source);
            }
            record.setQuantity(inportQuantity);
            record.setCreatetime(DateUtil.now());
            if (user != null) {
                record.setOperator(user.getName());
            }
            consumableRecordService.save(record);

            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }
    
    @RequestMapping("addStock")
    @RequiresPermissions("consumable:inport")
    public ResultObj addStock(Integer id, Integer quantity, String source) {
         try {
             User user = (User) WebUtils.getSession().getAttribute("user");
             Consumable consumable = consumableService.getById(id);
             if (consumable == null) return ResultObj.ERROR;
             
             consumable.setQuantity(consumable.getQuantity() + quantity);
             consumable.setUpdateTime(DateUtil.now());
             consumableService.updateById(consumable);
             
             // Record
             ConsumableRecord record = new ConsumableRecord();
             record.setConsumableId(consumable.getId());
             record.setType(StringUtils.isNotBlank(source) ? source : "采购入库");
             record.setQuantity(quantity);
             record.setCreatetime(DateUtil.now());
             if (user != null) {
                 record.setOperator(user.getName());
             }
             consumableRecordService.save(record);
             
             return ResultObj.OPERATE_SUCCESS;
         } catch (Exception e) {
             e.printStackTrace();
             return ResultObj.OPERATE_ERROR;
         }
    }

    @RequestMapping("batchImport")
    @RequiresPermissions("consumable:inport")
    public ResultObj batchImport(MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            reader.addHeaderAlias("耗材名称(必填)", "type");
            reader.addHeaderAlias("数量(必填)", "quantity");
            reader.addHeaderAlias("位置", "location");
            reader.addHeaderAlias("预警阈值", "threshold");
            
            List<Consumable> list = reader.readAll(Consumable.class);
            
            int success = 0;
            int fail = 0;
            User user = (User) WebUtils.getSession().getAttribute("user");
            String operator = (user != null) ? user.getName() : "System";

            for (Consumable item : list) {
                try {
                    if (StringUtils.isBlank(item.getType()) || item.getQuantity() == null) {
                        fail++; continue;
                    }

                    QueryWrapper<Consumable> qw = new QueryWrapper<>();
                    qw.eq("type", item.getType());
                    Consumable exist = consumableService.getOne(qw);
                    Integer qty = item.getQuantity();

                    if (exist != null) {
                        exist.setQuantity(exist.getQuantity() + qty);
                        consumableService.updateById(exist);
                        item.setId(exist.getId());
                    } else {
                        item.setUpdateTime(DateUtil.now());
                        item.setInsertTime(DateUtil.now());
                        if (item.getThreshold() == null) item.setThreshold(0);
                        consumableService.save(item);
                    }

                    // Record
                    ConsumableRecord record = new ConsumableRecord();
                    record.setConsumableId(item.getId());
                    record.setType("批量入库");
                    record.setQuantity(qty);
                    record.setCreatetime(DateUtil.now());
                    record.setOperator(operator);
                    record.setRemark("Excel批量导入");
                    consumableRecordService.save(record);

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
        writer.addHeaderAlias("type", "耗材名称(必填)");
        writer.addHeaderAlias("quantity", "数量(必填)");
        writer.addHeaderAlias("location", "位置");
        writer.addHeaderAlias("threshold", "预警阈值");
        writer.setOnlyAlias(true);
        
        List<Consumable> rows = new ArrayList<>();
        Consumable example = new Consumable();
        example.setType("A4纸");
        example.setQuantity(100);
        example.setLocation("办公区");
        example.setThreshold(10);
        rows.add(example);
        
        writer.write(rows, true);

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("耗材批量入库模板.xls", "UTF-8"));
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
