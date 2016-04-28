package org.devspark.aws.lorm.schema.validation;

import java.util.List;

import org.devspark.aws.lorm.schema.AttributeDefinition;
import org.devspark.aws.lorm.schema.EntitySchema;

public interface EntitySchemaSupport {

    List<SchemaValidationError> validateSchema(EntitySchema entitySchema);

    List<AttributeDefinition> getMissingFieldsInTable(EntitySchema entitySchema);

    List<AttributeDefinition> getMissingAttributesInEntityClass(
	    EntitySchema entitySchema);

    // TODO get missing indexes in entity

    // TODO get missing indexes in table

}
