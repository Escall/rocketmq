package com.example.order.mapping;

import com.example.store.mapping.StoreMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderMapperTest {

    @Autowired
    private StoreMapper storeMapper;

    @Test
    public void testSelectVersion() throws Exception {
        int version = storeMapper.selectVersion("1", "001");
        System.out.println(version);
    }

    @Test
    public void testStoreCount() throws Exception {
        int count = storeMapper.selectStoreCount("1", "001");
        System.out.println("count: " + count);
    }
}