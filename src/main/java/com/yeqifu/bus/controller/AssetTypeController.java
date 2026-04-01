package com.yeqifu.bus.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.AssetType;
import com.yeqifu.bus.service.IAssetTypeService;
import com.yeqifu.sys.common.DataGridView;
import com.yeqifu.sys.common.ResultObj;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("assetType")
public class AssetTypeController {

    @Autowired
    private IAssetTypeService assetTypeService;

    /**
     * 加载所有资产类型（用于下拉框）
     */
    @RequestMapping("loadAllAssetType")
    public DataGridView loadAllAssetType() {
        QueryWrapper<AssetType> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<AssetType> list = assetTypeService.list(queryWrapper);
        return new DataGridView(Long.valueOf(list.size()), list);
    }

    /**
     * 加载资产类型列表（用于管理表格）
     */
    @RequestMapping("loadAssetTypeManager")
    public DataGridView loadAssetTypeManager(AssetType assetType, Integer page, Integer limit) {
        Page<AssetType> pageInfo = new Page<>(page, limit);
        QueryWrapper<AssetType> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(assetType.getName()), "name", assetType.getName());
        queryWrapper.orderByDesc("create_time");
        IPage<AssetType> iPage = assetTypeService.page(pageInfo, queryWrapper);
        return new DataGridView(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 添加资产类型
     */
    @RequestMapping("addAssetType")
    public ResultObj addAssetType(AssetType assetType) {
        try {
            // Check uniqueness
            QueryWrapper<AssetType> qw = new QueryWrapper<>();
            qw.eq("name", assetType.getName());
            if (assetTypeService.count(qw) > 0) {
                return new ResultObj(-1, "该类型名称已存在");
            }
            
            // Generate ID manually because Oracle doesn't support AUTO without trigger/identity
            QueryWrapper<AssetType> orderQw = new QueryWrapper<>();
            orderQw.orderByDesc("id");
            Page<AssetType> page = new Page<>(1, 1);
            IPage<AssetType> resultPage = assetTypeService.page(page, orderQw);
            List<AssetType> list = resultPage.getRecords();
            Integer id = 1;
            if (list != null && list.size() > 0) {
                id = list.get(0).getId() + 1;
            }
            assetType.setId(id);

            assetType.setCreateTime(DateUtil.now());
            assetTypeService.save(assetType);
            return ResultObj.ADD_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.ADD_ERROR;
        }
    }

    /**
     * 修改资产类型
     */
    @RequestMapping("updateAssetType")
    public ResultObj updateAssetType(AssetType assetType) {
        try {
             // Check uniqueness excluding self
            QueryWrapper<AssetType> qw = new QueryWrapper<>();
            qw.eq("name", assetType.getName());
            qw.ne("id", assetType.getId());
            if (assetTypeService.count(qw) > 0) {
                return new ResultObj(-1, "该类型名称已存在");
            }
            
            assetTypeService.updateById(assetType);
            return ResultObj.UPDATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.UPDATE_ERROR;
        }
    }

    /**
     * 删除资产类型
     */
    @RequestMapping("deleteAssetType")
    public ResultObj deleteAssetType(Integer id) {
        try {
            assetTypeService.removeById(id);
            return ResultObj.DELETE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultObj.DELETE_ERROR;
        }
    }
}
