package org.devspark.aws.lorm.test.model.embedded;

import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SampleEntity {
    private String someRandomField;
    @Id
    private String id;

    @Embedded
    private SampleEmbeddable embedded;

    @Embedded
    private List<SampleEmbeddable> embeddeds;
    
    public String getSomeRandomField() {
	return someRandomField;
    }

    public void setSomeRandomField(String someRandomField) {
	this.someRandomField = someRandomField;
    }

    public SampleEmbeddable getEmbedded() {
	return embedded;
    }

    public void setEmbedded(SampleEmbeddable embedded) {
	this.embedded = embedded;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public void setEmbeddeds(List<SampleEmbeddable> embeddeds) {
        this.embeddeds = embeddeds;
    }
    
    public List<SampleEmbeddable> getEmbeddeds() {
        return embeddeds;
    }
    
}
