package com.yeqifu.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bus_check_asset")
@KeySequence(value = "SEQ_BUS_CHECK_ASSET", clazz = Integer.class)
public class CheckAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField("check_id")
    private Integer checkId;

    @TableField("asset_id")
    private Integer assetId;

    @TableField("expected_status")
    private String expectedStatus;

    @TableField("actual_status")
    private String actualStatus;

    @TableField("expected_location")
    private String expectedLocation;

    @TableField("actual_location")
    private String actualLocation;

    private String result;
    private String remark;
    
    @TableField(exist = false)
    private String assetName;
    @TableField(exist = false)
    private String assetNo;
    @TableField(exist = false)
    private String location;
    
    // Extra fields for export
    @TableField(exist = false)
    private String maintenanceId;
    @TableField(exist = false)
    private String assetType;
    @TableField(exist = false)
    private String brand;
    @TableField(exist = false)
    private String serialNo;
    @TableField(exist = false)
    private String createTime; // Inbound time
    @TableField(exist = false)
    private String assetOperator; // Inbound person (creator)
}
