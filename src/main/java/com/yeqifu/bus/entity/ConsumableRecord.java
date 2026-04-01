package com.yeqifu.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
// import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bus_consumable_record")
public class ConsumableRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer consumableId;
    private String type; // IN, OUT
    private Integer quantity;
    @TableField("date_time")
    private String createtime;
    private String applicant;
    private String operator;
    private String remark;

    @TableField(exist = false)
    private String consumableType;
    @TableField(exist = false)
    private String consumableLocation;
}
