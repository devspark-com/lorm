package org.devspark.aws.lorm;

import java.util.List;

import org.devspark.aws.lorm.schema.validation.SchemaValidationError;

public interface SchemaSupport<T> {

    boolean isValid(List<SchemaValidationError> errors);

    boolean syncToSchema(boolean deleteMissingFields, boolean createTableIfNotExists,
	    boolean createReferences);

}
