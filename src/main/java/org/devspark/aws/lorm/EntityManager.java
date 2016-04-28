package org.devspark.aws.lorm;

import org.devspark.aws.lorm.mapping.EntityToItemMapper;
import org.devspark.aws.lorm.mapping.ItemToEntityMapper;
import org.devspark.aws.lorm.schema.validation.EntitySchemaSupport;

public interface EntityManager {

    <T> void addEntity(Class<T> entityClass, EntityToItemMapper entityToItemMapper,
	    ItemToEntityMapper<T> itemToEntityMapper,
	    EntitySchemaSupport entitySchemaSupport);

    <T> Repository<T> getRepository(Class<T> entityClass);

}
