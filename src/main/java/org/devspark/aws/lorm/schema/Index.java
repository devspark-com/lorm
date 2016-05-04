package org.devspark.aws.lorm.schema;

import java.util.List;

public class Index {
    private List<String> attributeNames;
    private String name;

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.toString().equals(toString());
    }

    @Override
    public String toString() {
        return name.toString();
    }
    
}
