package com.example.payb.mapping;

import com.example.payb.domain.PlatformAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;


/**
 * TIME:2019/9/6
 * USER: EsCall
 * DESC:
 */
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
public class PlatformAccountMapperTest {
@Autowired
PlatformAccountMapper platformAccountMapper;
    @Test
    public void selectByPrimaryKey() {
        PlatformAccount pa=platformAccountMapper.selectByPrimaryKey("platform001");
        System.out.println(pa.toString());
    }

    @Test
    public void updateByPrimaryKeySelective() {
        PlatformAccount pa = platformAccountMapper
                .selectByPrimaryKey("platform001"); // 当前平台的一个账号
        pa.setCurrentBalance(pa.getCurrentBalance().add(BigDecimal.valueOf(100)));
        Date currentTime = new Date();
        pa.setVersion(pa.getVersion() + 1);
        pa.setDateTime(currentTime);
        pa.setUpdateTime(currentTime);
      int count=  platformAccountMapper.updateByPrimaryKeySelective(pa);
        System.err.println(count);
    }
}