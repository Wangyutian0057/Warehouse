package com.yeqifu.bus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yeqifu.bus.entity.Asset;
import com.yeqifu.bus.service.IAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预留标准API接口，供外部系统对接
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private IAssetService assetService;

    // 从 application.yml 中读取外部对接所需的 Token，若未配置则使用默认值
    @Value("${api.token:warehouse_api_secret_token}")
    private String apiToken;

    /**
     * 根据运维编号列表获取设备信息列表
     * @param request HTTP请求（用于鉴权）
     * @param maintenanceIds 运维编号列表
     * @return 设备信息列表
     */
    @PostMapping("/device/info")
    public Map<String, Object> getDeviceInfo(HttpServletRequest request, @RequestBody List<String> maintenanceIds) {
        Map<String, Object> result = new HashMap<>();

        // 1. 接口鉴权 (简单的Token鉴权，满足安全扫描基本要求)
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authHeader) || !apiToken.equals(authHeader)) {
            result.put("code", 401);
            result.put("msg", "未授权或Token无效");
            return result;
        }

        // 2. 参数校验
        if (maintenanceIds == null || maintenanceIds.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "运维编号列表不能为空");
            return result;
        }

        // 3. 业务查询
        try {
            QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("maintenance_id", maintenanceIds);
            List<Asset> assetList = assetService.list(queryWrapper);

            // 4. 数据格式化转换
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Asset asset : assetList) {
                Map<String, Object> item = new HashMap<>();
                item.put("maintenanceId", asset.getMaintenanceId()); // 运维编号
                item.put("type", asset.getType());                   // 设备类型
                item.put("brand", asset.getBrand());                 // 品牌
                item.put("serialNo", asset.getSerialNo());           // 序列号
                item.put("assetNo", asset.getAssetNo());             // 资产编号
                item.put("location", asset.getLocation());           // 位置
                item.put("status", asset.getStatus());               // 状态
                item.put("entryDate", asset.getEntryDate());         // 入库时间 (根据实体类属性，也可以是createTime)
                dataList.add(item);
            }

            result.put("code", 000);
            result.put("msg", "success");
            result.put("data", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "服务器内部错误");
        }

        return result;
    }
}
