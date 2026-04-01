package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import com.yeqifu.bus.entity.Consumable;
import com.yeqifu.bus.entity.ConsumableRecord;
import com.yeqifu.bus.service.IConsumableRecordService;
import com.yeqifu.bus.service.IConsumableService;
import com.yeqifu.sys.common.ResultObj;
import com.yeqifu.sys.common.WebUtils;
import com.yeqifu.sys.entity.User;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;

@RestController
@RequestMapping("consumableOutport")
public class ConsumableOutportController {

    @Autowired
    private IConsumableService consumableService;
    @Autowired
    private IConsumableRecordService consumableRecordService;

    @RequestMapping("addConsumableOutport")
    @RequiresPermissions("consumable:outport")
    public ResultObj addConsumableOutport(Integer consumableId, Integer quantity, String applicant, String remark, String type) {
        try {
            User user = (User) WebUtils.getSession().getAttribute("user");
            
            Consumable consumable = consumableService.getById(consumableId);
            if (consumable == null) return ResultObj.ERROR;
            
            if (consumable.getQuantity() < quantity) {
                return new ResultObj(-1, "库存不足，当前库存：" + consumable.getQuantity());
            }
            
            consumable.setQuantity(consumable.getQuantity() - quantity);
            consumable.setUpdateTime(DateUtil.now());
            consumableService.updateById(consumable);

            // Record
            ConsumableRecord record = new ConsumableRecord();
            record.setConsumableId(consumableId);
            record.setType(type != null ? type : "OUT");
            record.setQuantity(quantity);
            record.setCreatetime(DateUtil.now());
            if (user != null) {
                record.setOperator(user.getName());
            }
            record.setApplicant(applicant);
            record.setRemark(remark);
            consumableRecordService.save(record);

            if (consumable.getQuantity() < consumable.getThreshold()) {
                return new ResultObj(200, "出库成功，但库存已低于安全阈值！");
            }

            return ResultObj.OPERATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.OPERATE_ERROR;
        }
    }

    @RequestMapping("deleteOutport")
    @RequiresPermissions("consumable:outport")
    public ResultObj deleteOutport(Integer id) {
        try {
            ConsumableRecord record = consumableRecordService.getById(id);
            if (record != null) {
                // Revert Quantity
                Consumable consumable = consumableService.getById(record.getConsumableId());
                if (consumable != null) {
                    consumable.setQuantity(consumable.getQuantity() + record.getQuantity());
                    consumableService.updateById(consumable);
                }
                consumableRecordService.removeById(id);
                return ResultObj.DELETE_SUCCESS;
            }
            return ResultObj.DELETE_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }

    @RequestMapping("updateOutport")
    public ResultObj updateOutport(ConsumableRecord record) {
        try {
            consumableRecordService.updateById(record);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }
}
