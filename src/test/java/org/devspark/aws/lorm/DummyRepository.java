package org.devspark.aws.lorm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devspark.aws.lorm.Repository;
import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.id.EntityIdHandler;

public class DummyRepository<T> implements Repository<T> {

    private final Map<String, T> instances;
    private final Class<T> entityClass;

    public DummyRepository(Class<T> entityClass) {
	instances = new HashMap<String, T>();
	this.entityClass = entityClass;
    }

    @Override
    public T findOne(String id) {
	return instances.get(id);
    }

    @Override
    public List<T> findAll() {
	List<T> valuesAsList = new ArrayList<T>();
	valuesAsList.addAll(instances.values());
	return valuesAsList;
    }

    @Override
    public T save(T instance) {
	EntityIdHandler idHandler = new EntityIdHandler(entityClass);
	String id = idHandler.getIdValue(instance);
	if (id == null) {
	    String generatedId = idHandler.generateId(instance);
	    if (generatedId == null) {
		throw new DataValidationException("Entity id should not be null");
	    }
	    id = generatedId;
	    idHandler.setIdValue(instance, generatedId);
	}

	instances.put(id, instance);

	return instance;
    }

    @Override
    public List<T> save(List<T> instances) {
	for (T t : instances) {
	    save(t);
	}

	return instances;
    }

    @Override
    public void deleteById(String id) {
	instances.remove(id);
    }

    @Override
    public void deleteById(List<String> ids) {
	for (String id : ids) {
	    deleteById(id);
	}
    }

    @Override
    public Class<T> getEntityClass() {
	return entityClass;
    }
}
