package com.zju.lab.ct.orm.dao;

import com.zju.lab.ct.annotations.HandlerDao;
import com.zju.lab.ct.orm.annotations.Select;
import com.zju.lab.ct.orm.container.MapperHandlerResponse;
import io.vertx.core.Handler;

/**
 * Created by wuhaitao on 2016/3/25.
 */
@HandlerDao
public interface TestMapper {

    @Select("select * from ct where id = #{id}")
    void test(int id, Handler<MapperHandlerResponse> ctImageHandler);
}
