package org.devspark.aws.lorm.mapping;

import java.util.Map;

import org.devspark.aws.lorm.schema.AttributeDefinition;

public interface ItemToEntityMapper <E> {
	
	E map(Map<AttributeDefinition, Object> attributes);
	
}
