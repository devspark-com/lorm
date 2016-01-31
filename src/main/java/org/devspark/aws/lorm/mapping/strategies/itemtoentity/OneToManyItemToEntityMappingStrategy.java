package org.devspark.aws.lorm.mapping.strategies.itemtoentity;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import javax.persistence.OneToMany;

import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.schema.AttributeDefinition;

public class OneToManyItemToEntityMappingStrategy implements ItemToEntityMappingStrategy {

    @Override
    public boolean apply(Field field) {
	return field.getAnnotation(OneToMany.class) != null;
    }

    @Override
    public void map(Entry<AttributeDefinition, Object> itemEntry, Field field,
	    Object entityInstance) {
	throw new DataValidationException("OneToMany annotation is not supported");
    }

}
