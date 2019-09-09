package com.example.store.mapping;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * TIME:2019/9/4
 * USER: EsCall
 * DESC: sql
 */
@Mapper
public interface StoreMapper {

    @Select("select version from t_store where supplier_id=#{supplierId} and goods_id=#{goodsId}")
    int selectVersion(@Param("supplierId") String supplierId, @Param("goodsId") String goodsId);

    @Update(" update t_store ts\n" +
            "    set \n" +
            "      store_count = store_count - 1,\n" +
            "      version = version + 1,\n" +
            "      update_by = #{updateBy},\n" +
            "      update_time = #{updateTime}\n" +
            "    where  ts.supplier_id = #{supplierId}\n" +
            "    \t   and\n" +
            "    \t   ts.goods_id = #{goodsId}\n" +
            "    \t   and\n" +
            "    \t   ts.version = #{version}\n" +
            "    \t   and\t\n" +
            "  \t\t   ts.store_count > 0")
    int updateStoreCountByVersion(@Param("version") int version, @Param("supplierId") String supplierId,
                                  @Param("goodsId") String goodsId, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    @Select("select store_count from t_store ts\n" +
            "  \t\t\t\t\t   where ts.supplier_id = #{supplierId}\n" +
            "  \t\t\t\t\t         and\n" +
            "  \t\t\t\t\t         ts.goods_id = #{goodsId}")
    int selectStoreCount(@Param("supplierId") String supplierId, @Param("goodsId") String goodsId);

}
