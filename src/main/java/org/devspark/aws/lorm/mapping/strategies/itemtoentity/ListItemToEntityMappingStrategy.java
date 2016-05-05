package org.devspark.aws.lorm.mapping.strategies.itemtoentity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map.Entry;

import org.devspark.aws.lorm.ReflectionSupport;
import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.schema.AttributeDefinition;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ListItemToEntityMappingStrategy
        implements ItemToEntityMappingStrategy {

    private final ReflectionSupport reflectionSupport;

    public ListItemToEntityMappingStrategy(ReflectionSupport reflectionSupport) {
        super();
        this.reflectionSupport = reflectionSupport;
    }

    @Override
    public boolean apply(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    @Override
    public void map(Entry<AttributeDefinition, Object> itemEntry, Field field,
            Object entityInstance) {
        Object value = itemEntry.getValue();
        if (value == null) {
            return;
        }

        if (!String.class.equals(value.getClass())) {
            throw new DataValidationException("Incompatible item attribute ("
                    + field.getName() + "). " + "Expected: " + String.class.getName()
                    + ". Actual: " + value.getClass().getName());
        }

        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> listOfType = (Class<?>) listType.getActualTypeArguments()[0];
        JavaType type = TypeFactory.defaultInstance()
                .constructCollectionLikeType(List.class, listOfType);

        try {
            Object asObject = new ObjectMapper().readValue(((String) value).getBytes(),
                    type);
            reflectionSupport.setValueOfField(field, entityInstance, asObject);
        } catch (Exception e) {
            throw new DataValidationException("Error when deserializing "
                    + itemEntry.getKey() + " attribute as Collection for type "
                    + value.getClass().getName() + " from content: " + value.toString(),
                    e);
        }

    }

}
