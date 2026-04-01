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
@TableName("bus_check_consumable")
@KeySequence(value = "SEQ_BUS_CHECK_CONS", clazz = Integer.class)
public class CheckConsumable implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField("check_id")
    private Integer checkId;

    @TableField("consumable_id")
    private Integer consumableId;

    @TableField("expected_quantity")
    private Integer expectedQuantity;

    @TableField("actual_quantity")
    private Integer actualQuantity;

    @TableField("diff_quantity")
    private Integer diffQuantity;

    private String remark;
    
    @TableField(exist = false)
    private String consumableName; // Usually 'type' in Consumable entity
    @TableField(exist = false)
    private String location;
}
