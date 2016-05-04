package org.devspark.aws.lorm.schema;

import java.util.Set;

public class EntitySchema {
    private final String name;
    private final Set<AttributeDefinition> attributes;
    private final Set<Index> indexes;

    public EntitySchema(String name, Set<AttributeDefinition> attributes,
            Set<Index> indexes) {
        super();
        this.name = name;
        this.attributes = attributes;
        this.indexes = indexes;
    }

    public String getName() {
        return name;
    }

    public Set<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public Set<Index> getIndexes() {
        return indexes;
    }
}
