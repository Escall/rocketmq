package com.example.payb.mapping;

import com.example.payb.domain.PlatformAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * TIME:2019/9/5
 * USER: EsCall
 * DESC:
 */

@Mapper
public interface PlatformAccountMapper {
    @Select("select * from t_platform_account\n" +
            "    where account_id = #{accountId}")
    PlatformAccount selectByPrimaryKey(@Param("accountId") String accountId);

    @Update(" update t_platform_account set account_no = #{accountNo}," +
            "date_time = #{dateTime}, current_balance = #{currentBalance},version = #{version}," +
            " create_time = #{createTime},update_time = #{updateTime} where account_id = #{accountId}")
    int updateByPrimaryKeySelective(PlatformAccount record);
}
