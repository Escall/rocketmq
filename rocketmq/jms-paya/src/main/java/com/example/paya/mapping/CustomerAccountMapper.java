package com.example.paya.mapping;

import com.example.paya.domain.CustomerAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@Mapper
public interface CustomerAccountMapper {
    @Update("  update t_customer_account\n" +
            "    \t\tset current_balance = #{newBalance},\n" +
            "      \t\t\tversion = version + 1,\n" +
            "      \t\t\tupdate_time = #{updateTime}\n" +
            "    \t\twhere account_id = #{accountId} \n" +
            "          \t\t  and\n" +
            "          \t\t  version =  #{version}")
    int updateBalance(@Param("accountId") String accountId, @Param("newBalance") BigDecimal newBalance,
                      @Param("version") int currentVersion, @Param("updateTime") Date currentTime);

    @Select("select" +
            "  *  from t_customer_account\n" +
            "    where account_id = #{accountId}")
    CustomerAccount selectByPrimaryKey(String accountId);
}
