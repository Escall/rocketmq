package com.example.store.service;

import java.util.Date;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC: 库存接口，向外暴露
 */
public interface StoreSerivceApi {

    //查询版本号
    int selectVersion(String supplierId,String goodsId);
    //更新库存,供用商，货物id,由于使用乐观锁，所以需要version
    int updateStoreCountByVersion(int version, String supplierId, String goodsId, String updateBy, Date updateTime);

    //查询库存
    int selectStoreCount(String supplierId,String goodsId);
}
