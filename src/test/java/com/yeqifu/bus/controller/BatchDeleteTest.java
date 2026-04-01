package com.yeqifu.bus.controller;

import com.yeqifu.bus.service.IAssetService;
import com.yeqifu.bus.service.IConsumableService;
import com.yeqifu.sys.common.ResultObj;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class BatchDeleteTest {

    @Mock
    private IAssetService assetService;

    @Mock
    private IConsumableService consumableService;

    @InjectMocks
    private AssetController assetController;

    @InjectMocks
    private ConsumableController consumableController;

    @Test
    public void testBatchDeleteAssetSuccess() {
        Integer[] ids = {1, 2, 3};
        when(assetService.removeByIds(any(Collection.class))).thenReturn(true);

        ResultObj result = assetController.batchDeleteAsset(ids);

        assertEquals(ResultObj.DELETE_SUCCESS.getCode(), result.getCode());
        verify(assetService, times(1)).removeByIds(any(Collection.class));
    }

    @Test
    public void testBatchDeleteAssetFailNull() {
        ResultObj result = assetController.batchDeleteAsset(null);
        assertEquals(ResultObj.DELETE_ERROR.getCode(), result.getCode());
    }

    @Test
    public void testBatchDeleteConsumableSuccess() {
        Integer[] ids = {4, 5, 6};
        when(consumableService.removeByIds(any(Collection.class))).thenReturn(true);

        ResultObj result = consumableController.batchDeleteConsumable(ids);

        assertEquals(ResultObj.DELETE_SUCCESS.getCode(), result.getCode());
        verify(consumableService, times(1)).removeByIds(any(Collection.class));
    }

    @Test
    public void testBatchDeleteConsumableFailEmpty() {
        Integer[] ids = {};
        ResultObj result = consumableController.batchDeleteConsumable(ids);
        assertEquals(ResultObj.DELETE_ERROR.getCode(), result.getCode());
    }
}