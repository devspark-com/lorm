package org.devspark.aws.lorm.mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.devspark.aws.lorm.EntityManager;
import org.devspark.aws.lorm.ReflectionSupport;
import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.id.EntityIdHandler;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.ListItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.DateItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.DefaultItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.ItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.ManyToOneItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.OneToManyItemToEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.itemtoentity.TransientItemToEntityMappingStrategy;
import org.devspark.aws.lorm.schema.AttributeDefinition;

public class ItemToEntityMapperImpl<E> implements ItemToEntityMapper<E> {
    private final Class<E> entityClass;
    private final List<ItemToEntityMappingStrategy> mappingStrategies;

    public ItemToEntityMapperImpl(Class<E> entityClass, EntityManager entityManager) {
        super();
        this.entityClass = entityClass;

        ReflectionSupport reflectionSupport = new ReflectionSupport();

        mappingStrategies = new ArrayList<ItemToEntityMappingStrategy>();
        mappingStrategies.add(new TransientItemToEntityMappingStrategy());
        mappingStrategies.add(new OneToManyItemToEntityMappingStrategy());
        mappingStrategies.add(new ManyToOneItemToEntityMappingStrategy(reflectionSupport,
                entityManager));
        mappingStrategies.add(new DateItemToEntityMappingStrategy(reflectionSupport));
        mappingStrategies.add(new ListItemToEntityMappingStrategy(reflectionSupport));
        mappingStrategies.add(new DefaultItemToEntityMappingStrategy(reflectionSupport));
    }

    @SuppressWarnings("unchecked")
    @Override
    public E map(Map<AttributeDefinition, Object> attributes) {
        return (E) map(attributes, entityClass, "");
    }

    public Object map(Map<AttributeDefinition, Object> attributes,
            Class<?> entityClassToParse, String fieldNamePrefix) {
        ReflectionSupport reflectionSupport = new ReflectionSupport();

        List<Field> fields = reflectionSupport.getAllFields(entityClassToParse);
        Object instance = reflectionSupport.instantiate(entityClassToParse);
        for (Entry<AttributeDefinition, Object> itemEntry : attributes.entrySet()) {
            String fieldKey = itemEntry.getKey().getName();
            if (fieldKey.contains(".")) {
                // it has embedded objects
                if (fieldNamePrefix.isEmpty()) {
                    // just one level, just extract the field name
                    fieldKey = fieldKey.substring(0, fieldKey.indexOf('.'));
                } else {
                    // get the next level
                    if (fieldKey.indexOf('.', fieldNamePrefix.length()) > 0) {
                        fieldKey = fieldKey.substring(fieldNamePrefix.length(),
                                fieldKey.indexOf('.', fieldNamePrefix.length()));
                    } else {
                        if (fieldNamePrefix.length() < fieldKey.length()) {
                            fieldKey = fieldKey.substring(fieldNamePrefix.length());
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                // it does not have embedded objects
                if (!fieldNamePrefix.isEmpty()) {
                    // a prefix means that it should have at least a dot for
                    // separate
                    // embedded object and field.
                    // Just ignore this malformed attribute naming
                    continue;
                }
            }

            Field field = getFieldByName(fieldKey, fields);
            if (field == null) {
                // TODO how to support entity versions?
                // i.e.: how to deal with a removed field that was used before
                // in persisted instances?
                // For now, just skip it as it may correspond to some of the
                // embedded attributes
                continue;
            }

            if (field.getAnnotation(Embedded.class) != null && 
                    !Collection.class.isAssignableFrom(field.getType())) {
                if (field.getType().getAnnotation(Embeddable.class) != null) {
                    String embeddedFieldNamePrefix = fieldNamePrefix + field.getName()
                            + ".";
                    Object embeddedValue = map(attributes, field.getType(),
                            embeddedFieldNamePrefix);
                    reflectionSupport.setValueOfField(field, instance, embeddedValue);
                } else {
                    throw new DataValidationException("Error while mapping attributes to "
                            + entityClassToParse.getName() + " .Reason: "
                            + field.getType().getName()
                            + " is not embeddable neither a Collection");
                }
            } else {
                for (ItemToEntityMappingStrategy mappingStrategy : mappingStrategies) {
                    if (mappingStrategy.apply(field)) {
                        mappingStrategy.map(itemEntry, field, instance);
                        break;
                    }
                }

            }
        }

        if (entityClassToParse.getAnnotation(Entity.class) != null) {
            // validate primary key mapping
            EntityIdHandler idHandler = new EntityIdHandler(entityClassToParse);
            String id = idHandler.getIdValue(instance);
            if (id == null) {
                throw new DataValidationException("Primary key not found in item when "
                        + "mapping to entity " + entityClass.getName());
            }
        }

        return instance;
    }

    private Field getFieldByName(String fieldName, List<Field> fields) {
        Field field = null;

        for (Field currentField : fields) {
            if (currentField.getName().equals(fieldName)) {
                field = currentField;
                break;
            }
        }

        return field;
    }

}
