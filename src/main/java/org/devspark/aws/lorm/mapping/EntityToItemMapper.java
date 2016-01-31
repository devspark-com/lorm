package org.devspark.aws.lorm.mapping;

import java.util.Map;

import org.devspark.aws.lorm.schema.AttributeDefinition;

public interface EntityToItemMapper {

    Map<AttributeDefinition, Object> map(Object entity);

}
