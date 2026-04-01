package com.yeqifu.bus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.Consumable;
import com.yeqifu.bus.service.IConsumableService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.hutool.core.date.DateUtil;
import java.util.Arrays;
import java.util.Date;

@RestController
@RequestMapping("consumable")
public class ConsumableController {

    @Autowired
    private IConsumableService consumableService;

    @RequestMapping("loadAllConsumable")
    @RequiresPermissions("consumable:view")
    public DataGridView loadAllConsumable(Consumable consumable, Integer page, Integer limit, String startTime, String endTime) {
        Page<Consumable> pageInfo = new Page<>(page, limit);
        QueryWrapper<Consumable> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(consumable.getType())) {
            queryWrapper.like("type", consumable.getType());
        }
        if (StringUtils.isNotBlank(startTime)) {
            queryWrapper.ge("insert_time", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le("insert_time", endTime);
        }
        queryWrapper.orderByDesc("update_time");
        IPage<Consumable> iPage = consumableService.page(pageInfo, queryWrapper);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    @RequestMapping("addConsumable")
    @RequiresPermissions("consumable:create")
    public ResultObj addConsumable(Consumable consumable) {
        try {
            consumable.setUpdateTime(DateUtil.now());
            consumableService.save(consumable);
            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }

    @RequestMapping("updateConsumable")
    @RequiresPermissions("consumable:update")
    public ResultObj updateConsumable(Consumable consumable) {
        try {
            consumable.setUpdateTime(DateUtil.now());
            consumableService.updateById(consumable);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }

    @RequestMapping("deleteConsumable")
    @RequiresPermissions("consumable:delete")
    public ResultObj deleteConsumable(Integer id) {
        try {
            consumableService.removeById(id);
            return ResultObj.DELETE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }

    @RequestMapping("batchDeleteConsumable")
    @RequiresPermissions("consumable:delete")
    public ResultObj batchDeleteConsumable(Integer[] ids) {
        try {
            if (ids != null && ids.length > 0) {
                consumableService.removeByIds(Arrays.asList(ids));
                return ResultObj.DELETE_SUCCESS;
            } else {
                return ResultObj.DELETE_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }
    
    @RequestMapping("loadWarningConsumable")
    public DataGridView loadWarningConsumable(Integer page, Integer limit) {
         Page<Consumable> pageInfo = new Page<>(page, limit);
         QueryWrapper<Consumable> queryWrapper = new QueryWrapper<>();
         queryWrapper.apply("quantity < threshold");
         IPage<Consumable> iPage = consumableService.page(pageInfo, queryWrapper);
         return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    @RequestMapping("loadConsumableCategoryStat")
    public DataGridView loadConsumableCategoryStat() {
        return new DataGridView(Long.valueOf(0), consumableService.loadConsumableCategoryStat());
    }
    
    @RequestMapping("loadConsumableCount")
    public ResultObj loadConsumableCount() {
        return new ResultObj(200, String.valueOf(consumableService.count()));
    }
}
