package com.yeqifu.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeqifu.bus.entity.CheckConsumable;
import com.yeqifu.bus.mapper.CheckConsumableMapper;
import com.yeqifu.bus.service.ICheckConsumableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CheckConsumableServiceImpl extends ServiceImpl<CheckConsumableMapper, CheckConsumable> implements ICheckConsumableService {

    @Autowired
    private CheckConsumableMapper checkConsumableMapper;

    @Override
    public List<CheckConsumable> queryCheckConsumables(Integer checkId) {
        return checkConsumableMapper.queryCheckConsumables(checkId);
    }
}
