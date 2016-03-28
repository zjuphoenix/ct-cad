package com.zju.lab.ct.orm;

import com.zju.lab.ct.orm.container.MapperHandlerResponse;
import com.zju.lab.ct.orm.dao.TestMapper;
import io.vertx.core.Handler;

import java.util.ArrayList;

/**
 * Created by wuhaitao on 2016/3/26.
 */
public class TestMapperImpl implements TestMapper {
    @Override
    public void test(int id, Handler<MapperHandlerResponse> ctImageHandler) {
        MapperHandlerResponse mapperHandlerResponse = new MapperHandlerResponse();
        mapperHandlerResponse.setResult(new ArrayList<>());
        ctImageHandler.handle(mapperHandlerResponse);
    }
}
