package com.yeqifu.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeqifu.bus.entity.Consumable;
import java.util.List;
import java.util.Map;

public interface IConsumableService extends IService<Consumable> {
    List<Map<String, Object>> loadConsumableCategoryStat();
}
