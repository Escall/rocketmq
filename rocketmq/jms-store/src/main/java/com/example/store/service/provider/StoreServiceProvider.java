package com.example.store.service.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.store.mapping.StoreMapper;
import com.example.store.service.StoreSerivceApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:dubbo服务实际提供者,将实际服务者注册到dubbo
 */

@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}")
public class StoreServiceProvider implements StoreSerivceApi {
    @Autowired
    private StoreMapper storeMapper;
    @Override
    public int selectVersion(String supplierId, String goodsId) {

        return storeMapper.selectVersion(supplierId,goodsId);
    }

    @Override
    public int updateStoreCountByVersion(int version, String supplierId, String goodsId, String updateBy, Date updateTime) {
        return storeMapper.updateStoreCountByVersion(version,supplierId,goodsId,updateBy,updateTime);
    }

    @Override
    public int selectStoreCount(String supplierId, String goodsId) {
        return storeMapper.selectStoreCount(supplierId,goodsId);
    }
}
