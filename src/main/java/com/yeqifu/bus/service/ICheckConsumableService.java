package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.CheckConsumable;
import java.util.List;

public interface ICheckConsumableService extends IService<CheckConsumable> {
    List<CheckConsumable> queryCheckConsumables(Integer checkId);
}
