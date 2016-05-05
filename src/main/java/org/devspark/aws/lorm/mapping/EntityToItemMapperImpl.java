package org.devspark.aws.lorm.mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.devspark.aws.lorm.ReflectionSupport;
import org.devspark.aws.lorm.exceptions.DataValidationException;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.ListEntityToItemMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.DateEntityToItemMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.DefaultEntityToItemMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.EntityToItemMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.ManyToOneEntityToItemMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.OneToManyEntityMappingStrategy;
import org.devspark.aws.lorm.mapping.strategies.entitytoitem.TransientEntityMappingStrategy;
import org.devspark.aws.lorm.schema.AttributeDefinition;
import org.devspark.aws.lorm.schema.EntitySchema;
import org.devspark.aws.lorm.schema.validation.EntityFieldAsAttribute;
import org.devspark.aws.lorm.schema.validation.EntitySchemaSupport;
import org.devspark.aws.lorm.schema.validation.SchemaValidationError;

public class EntityToItemMapperImpl<T>
        implements EntityToItemMapper, EntitySchemaSupport {

    private final Class<T> entityClass;
    private final List<EntityToItemMappingStrategy> mappingStrategies;
    private final Validator validator;

    public EntityToItemMapperImpl(Class<T> entityClass) {

        super();
        this.entityClass = entityClass;

        ReflectionSupport reflectionSupport = new ReflectionSupport();
        this.mappingStrategies = new ArrayList<EntityToItemMappingStrategy>();
        mappingStrategies.add(new TransientEntityMappingStrategy());
        mappingStrategies.add(new OneToManyEntityMappingStrategy());
        mappingStrategies
                .add(new ManyToOneEntityToItemMappingStrategy(reflectionSupport));
        mappingStrategies.add(new DateEntityToItemMappingStrategy(reflectionSupport));
        mappingStrategies.add(new ListEntityToItemMappingStrategy(reflectionSupport));
        mappingStrategies.add(new DefaultEntityToItemMappingStrategy(reflectionSupport));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    public Map<AttributeDefinition, Object> map(Object entity) {
        return map(entity, "", "");
    }

    private Map<AttributeDefinition, Object> map(Object entity, String fieldNamePrefix,
            String sourceFieldNamePrefix) {
        ReflectionSupport reflectionSupport = new ReflectionSupport();
        Map<AttributeDefinition, Object> attributes = new HashMap<AttributeDefinition, Object>();

        if (entity == null) {
            return attributes;
        }

        // it is assumed that entity will be validated before
        // mapping to Item

        List<Field> fields = reflectionSupport.getAllFields(entity.getClass());
        for (Field field : fields) {
            if (field.getAnnotation(Embedded.class) != null
                    && !field.getType().isAssignableFrom(List.class)) {
                if (field.getType().getAnnotation(Embeddable.class) == null) {
                    throw new DataValidationException(
                            "Error while mapping " + entityClass.getName() + " .Reason: "
                                    + field.getType().getName()
                                    + " is not embeddable neither a List");
                }

                String embeddedFieldamePrefix = fieldNamePrefix + field.getName() + ".";
                Object embedded = reflectionSupport.getValueOfField(field, entity);
                attributes.putAll(
                        map(embedded, embeddedFieldamePrefix, embeddedFieldamePrefix));

            } else {
                for (EntityToItemMappingStrategy mappingStrategy : mappingStrategies) {
                    if (mappingStrategy.apply(field)) {
                        mappingStrategy.map(entity, field, fieldNamePrefix, attributes);
                        assertFieldConstraints(entity, field.getName());

                        break;
                    }

                }
            }
        }

        return attributes;

    }

    private void assertFieldConstraints(Object entity, String attribute) {
        Set<ConstraintViolation<Object>> constraintViolations = validator
                .validateProperty(entity, attribute);
        if (constraintViolations.isEmpty()) {
            // valid
            return;
        }

        // not valid
        throw new DataValidationException("Invalid value for attribute: " + attribute
                + " .Reason: " + constraintViolations.iterator().next().getMessage());

    }

    @Override
    public List<SchemaValidationError> validateSchema(EntitySchema entitySchema) {
        List<Class<?>> dependenciesPath = new ArrayList<Class<?>>();
        return validateSchema(entitySchema, entityClass, "", dependenciesPath);
    }

    private List<SchemaValidationError> validateSchema(EntitySchema entitySchema,
            Class<?> entityClassToParse, String fieldNamePrefix,
            List<Class<?>> dependenciesPath) {

        List<SchemaValidationError> errors = new ArrayList<SchemaValidationError>();

        ReflectionSupport reflectionSupport = new ReflectionSupport();
        List<Field> fields = reflectionSupport.getAllFields(entityClassToParse);
        for (Field field : fields) {
            // TODO validate Lists
            if (List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            
            if (field.getAnnotation(Embedded.class) != null) {
                if (field.getType().getAnnotation(Embeddable.class) != null) {
                    // TODO move field naming to an strategy?
                    String embeddedFieldamePrefix = fieldNamePrefix + field.getName()
                            + ".";
                    if (dependenciesPath.contains(field.getType())) {
                        errors.add(SchemaValidationError.buildRecursiveError(entityClass,
                                entityClassToParse, field.getName()));
                        continue;
                    }

                    List<Class<?>> embeddedDependenciesPath = new ArrayList<Class<?>>();
                    embeddedDependenciesPath.addAll(dependenciesPath);
                    embeddedDependenciesPath.add(field.getType());

                    List<SchemaValidationError> currentErrors = validateSchema(
                            entitySchema, field.getType(), embeddedFieldamePrefix,
                            embeddedDependenciesPath);
                    if (currentErrors != null && !currentErrors.isEmpty()) {
                        errors.addAll(currentErrors);
                    }
                } else {
                    throw new DataValidationException(
                            "Error while mapping " + entityClass.getName() + " .Reason: "
                                    + field.getType().getName() + " is not embeddable");
                }
            } else {
                for (EntityToItemMappingStrategy mappingStrategy : mappingStrategies) {
                    if (mappingStrategy.apply(field)) {
                        List<SchemaValidationError> currentErrors = mappingStrategy
                                .hasValidSchema(entitySchema, entityClass, field,
                                        fieldNamePrefix);
                        if (currentErrors != null && !currentErrors.isEmpty()) {
                            errors.addAll(currentErrors);
                        }

                        break;
                    }
                }
            }
        }

        return errors;
    }

    @Override
    public List<AttributeDefinition> getMissingAttributesInEntityClass(
            EntitySchema entitySchema) {
        List<EntityFieldAsAttribute> entityFieldsAsAttributes = getEntityFieldsAsAttributes(
                entitySchema, entityClass, "");

        List<AttributeDefinition> missingAttrDefs = new ArrayList<AttributeDefinition>();

        Set<AttributeDefinition> attrDefs = entitySchema.getAttributes();
        for (AttributeDefinition attributeDefinition : attrDefs) {
            boolean found = false;
            for (EntityFieldAsAttribute entityFieldAsAttr : entityFieldsAsAttributes) {
                if (entityFieldAsAttr.getAttributeName()
                        .equals(attributeDefinition.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                missingAttrDefs.add(attributeDefinition);
            }
        }

        return missingAttrDefs;
    }

    private List<EntityFieldAsAttribute> getEntityFieldsAsAttributes(
            EntitySchema entitySchema, Class<?> entityClassToParse,
            String fieldNamePrefix) {
        List<EntityFieldAsAttribute> entityFieldsAsAttributes = new ArrayList<EntityFieldAsAttribute>();
        ReflectionSupport reflectionSupport = new ReflectionSupport();
        List<Field> fields = reflectionSupport.getAllFields(entityClassToParse);
        for (Field field : fields) {
            if (field.getAnnotation(Embedded.class) != null) {
                if (field.getType().getAnnotation(Embeddable.class) != null) {
                    String embeddedFieldamePrefix = fieldNamePrefix + field.getName()
                            + ".";
                    entityFieldsAsAttributes.addAll(getEntityFieldsAsAttributes(
                            entitySchema, field.getType(), embeddedFieldamePrefix));
                } else {
                    throw new DataValidationException(
                            "Error while parsing " + entityClass.getName() + " .Reason: "
                                    + field.getType().getName() + " is not embeddable");

                }
            } else {
                for (EntityToItemMappingStrategy mappingStrategy : mappingStrategies) {
                    if (mappingStrategy.apply(field)) {
                        List<EntityFieldAsAttribute> fieldAsAttr = mappingStrategy
                                .getEntityFieldAsAttribute(field, fieldNamePrefix);
                        if (fieldAsAttr != null) {
                            entityFieldsAsAttributes.addAll(fieldAsAttr);
                        }

                        break;
                    }
                }
            }
        }

        return entityFieldsAsAttributes;
    }

    @Override
    public List<AttributeDefinition> getMissingFieldsInTable(EntitySchema entitySchema) {
        return getMissingFieldsInTable(entitySchema, entityClass, "");
    }

    private List<AttributeDefinition> getMissingFieldsInTable(EntitySchema entitySchema,
            Class<?> entityClassToParse, String fieldNamePrefix) {
        List<AttributeDefinition> attrDefs = new ArrayList<AttributeDefinition>();

        ReflectionSupport reflectionSupport = new ReflectionSupport();
        List<Field> fields = reflectionSupport.getAllFields(entityClassToParse);
        for (Field field : fields) {
            if (field.getAnnotation(Embedded.class) != null) {
                if (field.getType().getAnnotation(Embeddable.class) != null) {
                    String embeddedFieldamePrefix = fieldNamePrefix + field.getName()
                            + ".";
                    List<AttributeDefinition> currentAttrDefs = getMissingFieldsInTable(
                            entitySchema, field.getType(), embeddedFieldamePrefix);
                    if (currentAttrDefs != null && !currentAttrDefs.isEmpty()) {
                        attrDefs.addAll(currentAttrDefs);
                    }
                } else {
                    throw new DataValidationException(
                            "Error while parsing " + entityClass.getName() + " .Reason: "
                                    + field.getType().getName() + " is not embeddable");
                }
            } else {
                for (EntityToItemMappingStrategy mappingStrategy : mappingStrategies) {
                    if (mappingStrategy.apply(field)) {
                        List<AttributeDefinition> currentAttrDefs = mappingStrategy
                                .getSchemaUpdate(entitySchema, entityClass, field,
                                        fieldNamePrefix);
                        if (currentAttrDefs != null && !currentAttrDefs.isEmpty()) {
                            attrDefs.addAll(currentAttrDefs);
                        }
                        break;
                    }
                }
            }
        }

        return attrDefs;
    }

}
