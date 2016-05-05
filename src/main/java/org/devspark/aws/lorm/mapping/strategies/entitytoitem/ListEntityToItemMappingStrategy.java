package org.devspark.aws.lorm.mapping.strategies.entitytoitem;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.persistence.Embedded;

import org.devspark.aws.lorm.ReflectionSupport;
import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.schema.AttributeDefinition;
import org.devspark.aws.lorm.schema.AttributeType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListEntityToItemMappingStrategy
        extends DefaultEntityToItemMappingStrategy {

    public ListEntityToItemMappingStrategy(ReflectionSupport reflectionSupport) {
        super(reflectionSupport);
    }

    @Override
    public boolean apply(Field field) {
        return List.class.isAssignableFrom(field.getType())
                && field.getAnnotation(Embedded.class) != null;
    }

    @Override
    protected boolean checkAttribute(Field field, AttributeType attrType) {
        return attrType.equals(AttributeType.STRING);
    }

    @Override
    protected AttributeDefinition buildAttributeDefinition(Field field,
            String fieldNamePrefix) {
        return new AttributeDefinition(fieldNamePrefix + field.getName(),
                AttributeType.STRING, null);
    }

    @Override
    public void map(Object entity, Field field, String fieldNamePrefix,
            Map<AttributeDefinition, Object> attributes) {
        AttributeDefinition attrDef = buildAttributeDefinition(field, fieldNamePrefix);
        Object value = reflectionSupport.getValueOfField(field, entity);
        if (value == null) {
            attributes.put(attrDef, null);
            return;
        }

        if (!List.class.isAssignableFrom(field.getType())) {
            throw new DataValidationException("Incompatible field type ("
                    + field.getName() + "). " + "Expected: " + List.class.getName()
                    + ". Actual: " + value.getClass().getName());
        }

        try {
            String asJson = new ObjectMapper().writeValueAsString(value);
            attributes.put(attrDef, asJson);
        } catch (JsonProcessingException e) {
            throw new DataValidationException(
                    "Error while serializing " + field.getName(), e);
        }

    }

}
