package org.devspark.aws.lorm.mapping.strategies.itemtoentity;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.devspark.aws.lorm.ReflectionSupport;
import org.devspark.aws.lorm.schema.AttributeDefinition;

public class DefaultItemToEntityMappingStrategy implements
		ItemToEntityMappingStrategy {

	private final ReflectionSupport reflectionSupport;

	public DefaultItemToEntityMappingStrategy(
			ReflectionSupport reflectionSupport) {
		super();
		this.reflectionSupport = reflectionSupport;
	}

	@Override
	public boolean apply(Field field) {
		return true;
	}

	@Override
	public void map(Entry<AttributeDefinition, Object> itemEntry, Field field,
			Object entityInstance) {
		reflectionSupport.setValueOfField(field, entityInstance,
				itemEntry.getValue());
	}
}
