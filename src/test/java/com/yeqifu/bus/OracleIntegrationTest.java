package com.yeqifu.bus;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeqifu.bus.entity.AssetRecord;
import com.yeqifu.bus.mapper.AssetRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OracleIntegrationTest {

    @Autowired
    private AssetRecordMapper assetRecordMapper;

    /**
     * 测试 Oracle 分页和日期查询
     * 对应 SQL: 
     * SELECT * FROM bus_asset_record r LEFT JOIN bus_asset a ON r.asset_id = a.id
     * WHERE r.date >= TO_DATE('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS')
     */
    @Test
    public void testQueryAssetRecord() {
        AssetRecord record = new AssetRecord();
        String startTime = "2020-01-01 00:00:00";
        String endTime = "2025-12-31 23:59:59";
        
        // 这里的参数会传递给 XML，XML 中已配置了 TO_DATE 转换
        Page<AssetRecord> page = new Page<>(1, 10);
        IPage<AssetRecord> resultPage = assetRecordMapper.queryAll(page, record, startTime, endTime, null);
        List<AssetRecord> list = resultPage.getRecords();
        
        System.out.println("查询结果数量: " + list.size());
        if (list.size() > 0) {
            System.out.println("第一条记录: " + list.get(0));
        }
    }
}
