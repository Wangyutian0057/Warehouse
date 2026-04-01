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
@TableName("bus_check")
@KeySequence(value = "SEQ_BUS_CHECK", clazz = Integer.class)
public class Check implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    private String title;
    private String remark;
    
    @TableField("create_time")
    private String createTime;
    
    @TableField("finish_time")
    private String finishTime;
    
    @TableField("oper_name")
    private String operName;
    
    private Integer status; // 0: Draft, 1: Finished
}
