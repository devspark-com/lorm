package org.devspark.aws.lorm;

import java.util.List;

public interface Repository<T> {

    T findOne(String id);

    // TODO add page support
    List<T> findAll();

    List<T> query(String attributeName, String value);

    T save(T instance);

    List<T> save(List<T> instances);

    void deleteById(String id);

    void deleteById(List<String> ids);

    Class<T> getEntityClass();
}