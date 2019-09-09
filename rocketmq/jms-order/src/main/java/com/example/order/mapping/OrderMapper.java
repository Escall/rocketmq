package com.example.order.mapping;

import com.example.order.domain.Order;
import org.apache.ibatis.annotations.*;

import java.util.Date;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC:
 */
@Mapper
public interface OrderMapper {
    @Insert(" insert into t_order (order_id, order_type, city_id, \n" +
            "      platform_id, user_id, supplier_id, \n" +
            "      goods_id, order_status, remark, \n" +
            "      create_by, create_time, update_by, \n" +
            "      update_time)\n" +
            "    values (#{orderId}, #{orderType}, #{cityId}, \n" +
            "      #{platformId}, #{userId}, #{supplierId}, \n" +
            "      #{goodsId}, #{orderStatus}, #{remark}, \n" +
            "      #{createBy}, #{createTime}, #{updateBy}, \n" +
            "      #{updateTime})")
    /**
     * statement="":表示定义的子查询语句
     * before=true：表示在之前执行，booler类型的,所以为true
     * keyColumn="myNo":表示查询所返回的类名
     * resultType=int.class：表示返回值得类型
     * keyProperty="empNo" ：表示将该查询的属性设置到某个列中，此处设置到empNo中
     *   //进行添加、修改等操作的时候只能返回数字，而不能返回java类或其他！
     *   // SELECT LAST_INSERT_ID()  适合那种主键是自增的类型
     *   // 1  insert语句需要写id字段了，并且 values里面也不能省略
     *   // 2 selectKey 的order属性需要写成BEFORE 因为这样才能将生成的uuid主键放入到model中，
     *   // 这样后面的insert的values里面的id才不会获取为空
     *   // 跟自增主键相比就这点区别，当然了这里的获取主键id的方式为 select uuid()
     */
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select last_insert_id()")
    long insertSelective(Order record);

    @Update("update t_order set order_status=#{status},update_by=#{updateBy},update_time=#{updateTime} where order_id=#{orderId}")
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    @Select("select *from t_order\n" +
            "    where order_id = #{orderId}")
    Order selectByPrimaryKey(String orderId);
}
