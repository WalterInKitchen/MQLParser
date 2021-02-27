package org.walterinkitchen.parser;

import java.util.List;

/**
 * 该接口用来查询数据库
 */
public interface MongoProvider {

    /**
     * 查询
     *
     * @param ql         查询语句
     * @param outputType 查询结果的类型
     * @param <T>        类
     * @return 查询结果
     */
    <T> List<T> query(String ql, Class<T> outputType);
}
