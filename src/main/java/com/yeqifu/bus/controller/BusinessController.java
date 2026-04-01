package com.yeqifu.bus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("bus")
public class BusinessController {

    @RequestMapping("toAssetManager")
    public String toAssetManager() {
        return "business/asset/assetManager";
    }

    @RequestMapping("toConsumableManager")
    public String toConsumableManager() {
        return "business/consumable/consumableManager";
    }

    @RequestMapping("toAssetInportManager")
    public String toAssetInportManager() {
        return "business/inport/assetInportManager";
    }

    @RequestMapping("toConsumableInportManager")
    public String toConsumableInportManager() {
        return "business/inport/consumableInportManager";
    }

    @RequestMapping("toAssetOutportManager")
    public String toAssetOutportManager() {
        return "business/outport/assetOutportManager";
    }

    @RequestMapping("toConsumableOutportManager")
    public String toConsumableOutportManager() {
        return "business/outport/consumableOutportManager";
    }

    @RequestMapping("toInventoryCheck")
    public String toInventoryCheck() {
        return "business/inventory/inventoryCheck";
    }

    @RequestMapping("toInventoryWarning")
    public String toInventoryWarning() {
        return "business/inventory/inventoryWarning";
    }

    @RequestMapping("toInventoryQuery")
    public String toInventoryQuery() {
        return "business/inventory/inventoryQuery";
    }

    @RequestMapping("toAssetTypeManager")
    public String toAssetTypeManager() {
        return "business/asset/assetTypeManager";
    }
}
