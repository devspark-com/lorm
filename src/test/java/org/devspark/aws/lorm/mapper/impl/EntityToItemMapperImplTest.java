package org.devspark.aws.lorm.mapper.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.devspark.aws.lorm.mapping.EntityToItemMapperImpl;
import org.devspark.aws.lorm.schema.AttributeConstraint;
import org.devspark.aws.lorm.schema.AttributeDefinition;
import org.devspark.aws.lorm.schema.AttributeType;
import org.devspark.aws.lorm.schema.EntitySchema;
import org.devspark.aws.lorm.schema.validation.SchemaValidationError;
import org.devspark.aws.lorm.schema.validation.SchemaValidationErrorType;
import org.devspark.aws.lorm.schema.validation.SchemaValidationScope;
import org.devspark.aws.lorm.test.model.Category;
import org.devspark.aws.lorm.test.model.Expense;
import org.devspark.aws.lorm.test.model.ExpenseType;
import org.devspark.aws.lorm.test.model.Merchant;
import org.devspark.aws.lorm.test.model.Reporter;
import org.devspark.aws.lorm.test.model.embedded.SampleEmbeddable;
import org.devspark.aws.lorm.test.model.embedded.SampleEntity;
import org.junit.Assert;
import org.junit.Test;

public class EntityToItemMapperImplTest {

    private AttributeDefinition buildAttrDefByName(String name) {
        return new AttributeDefinition(name, null, null);
    }

    @Test
    public void testSimpleMap() {
        EntityToItemMapperImpl<Merchant> mapper = new EntityToItemMapperImpl<Merchant>(
                Merchant.class);

        Merchant merchant = new Merchant();
        merchant.setId("123456");
        merchant.setName("something");

        Map<AttributeDefinition, Object> attrs = mapper.map(merchant);

        Assert.assertEquals("123456", attrs.get(buildAttrDefByName("id")));
        Assert.assertEquals("something", attrs.get(buildAttrDefByName("name")));
    }

    @Test
    public void testManyToOneMap() {
        EntityToItemMapperImpl<Expense> mapper = new EntityToItemMapperImpl<Expense>(
                Expense.class);

        Reporter reporter = new Reporter();
        reporter.setId("reporterid");
        reporter.setName("reportername");

        Category category = new Category();
        category.setId("categoryid");
        category.setDescription("categorydescription");

        Merchant merchant = new Merchant();
        merchant.setId("merchantid");
        merchant.setName("merchantdescription");

        Expense expense = new Expense();
        expense.setId("expenseid");
        expense.setDescription("expensedescription");
        expense.setMerchant(merchant);
        expense.setCategory(category);
        expense.setReporter(reporter);
        expense.setExpenseType(ExpenseType.NOT_REIMBURSABLE);
        expense.setAmount(new BigDecimal(10));
        long now = System.currentTimeMillis();
        expense.setDate(new Date(now));
        expense.setExpenseType(ExpenseType.REIMBURSABLE);

        Map<AttributeDefinition, Object> attrs = mapper.map(expense);
        Assert.assertEquals("expenseid", attrs.get(buildAttrDefByName("id")));
        Assert.assertEquals("expensedescription",
                attrs.get(buildAttrDefByName("description")));
        Assert.assertEquals("merchantid", attrs.get(buildAttrDefByName("merchant.id")));
        Assert.assertEquals("categoryid", attrs.get(buildAttrDefByName("category.id")));
        Assert.assertEquals("reporterid", attrs.get(buildAttrDefByName("reporter.id")));
        Assert.assertEquals("REIMBURSABLE", attrs.get(buildAttrDefByName("expenseType")));
        Assert.assertEquals(new BigDecimal(10), attrs.get(buildAttrDefByName("amount")));
        Assert.assertEquals(now, attrs.get(buildAttrDefByName("date")));
        Assert.assertEquals("REIMBURSABLE",
                attrs.get(buildAttrDefByName("expenseType")));
    }

    @Test
    public void validateSchema() {
        Set<AttributeConstraint> constraints = new HashSet<AttributeConstraint>();
        constraints.add(AttributeConstraint.buildPrimaryKeyConstraint());

        Set<org.devspark.aws.lorm.schema.AttributeDefinition> attributes = new HashSet<org.devspark.aws.lorm.schema.AttributeDefinition>();

        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("id",
                AttributeType.STRING, constraints));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("amount",
                AttributeType.NUMBER, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("category.id",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("date",
                AttributeType.NUMBER, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("description",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("merchant.id",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("reporter.id",
                AttributeType.STRING, null));

        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "attachment.location", AttributeType.STRING, null));

        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "attachment.description", AttributeType.STRING, null));

        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("expenseType",
                AttributeType.STRING, null));

        EntityToItemMapperImpl<Expense> mapper = new EntityToItemMapperImpl<Expense>(
                Expense.class);

        List<SchemaValidationError> positiveCaseValidationErrors = mapper
                .validateSchema(new EntitySchema("expense", attributes, null));
        Assert.assertTrue(positiveCaseValidationErrors.isEmpty());

        List<AttributeDefinition> missingFieldsInEntityClass = mapper
                .getMissingAttributesInEntityClass(
                        new EntitySchema("expense", attributes, null));
        Assert.assertTrue(missingFieldsInEntityClass.isEmpty());

        List<AttributeDefinition> missingFieldsInTable = mapper
                .getMissingAttributesInEntityClass(
                        new EntitySchema("expense", attributes, null));
        Assert.assertTrue(missingFieldsInTable.isEmpty());

        attributes.clear();
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("id",
                AttributeType.STRING, constraints));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("category.id",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("description",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("merchant.id",
                AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "attachment.description", AttributeType.STRING, null));

        List<SchemaValidationError> negativeCaseValidationErrors = mapper
                .validateSchema(new EntitySchema("expense", attributes, null));
        Assert.assertFalse(negativeCaseValidationErrors.isEmpty());
        Assert.assertEquals(5, negativeCaseValidationErrors.size());

        for (SchemaValidationError error : negativeCaseValidationErrors) {
            String attrName = error.getAttributeDefinition().getName();
            Assert.assertTrue(attrName.equals("reporter.id") || attrName.equals("amount")
                    || attrName.equals("date") || attrName.equals("attachment.location")
                    || attrName.equals("expenseType"));
        }

        List<AttributeDefinition> negativeMissingFieldsInEntityClass = mapper
                .getMissingAttributesInEntityClass(
                        new EntitySchema("expense", attributes, null));
        Assert.assertTrue(negativeMissingFieldsInEntityClass.isEmpty());

        List<AttributeDefinition> negativeFieldsInTable = mapper
                .getMissingFieldsInTable(new EntitySchema("expense", attributes, null));
        Assert.assertFalse(negativeFieldsInTable.isEmpty());
        Assert.assertEquals(5, negativeFieldsInTable.size());

        for (AttributeDefinition attrDef : negativeFieldsInTable) {
            Assert.assertTrue(attrDef.getName().equals("reporter.id")
                    || attrDef.getName().equals("amount")
                    || attrDef.getName().equals("date")
                    || attrDef.getName().equals("attachment.location")
                    || attrDef.getName().equals("expenseType"));
        }

    }

    @Test
    public void testDeepEmbeddedMap() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setSomeRandomField("random field");
        sampleEntity.setEmbedded(buildSampleEmbeddable(5));

        List<SampleEmbeddable> embeddeds = new ArrayList<>();
        embeddeds.add(buildSampleEmbeddable(1));
        embeddeds.add(buildSampleEmbeddable(1));
        embeddeds.add(buildSampleEmbeddable(1));

        sampleEntity.setEmbeddeds(embeddeds);

        EntityToItemMapperImpl<SampleEntity> mapper = new EntityToItemMapperImpl<SampleEntity>(
                SampleEntity.class);

        Map<AttributeDefinition, Object> attrs = mapper.map(sampleEntity);

        String attrName = "embedded";
        for (int i = 5; i > 0; i--) {
            AttributeDefinition expectedAttr = new AttributeDefinition(
                    attrName + ".someField", AttributeType.STRING, null);
            Assert.assertTrue(attrs.containsKey(expectedAttr));
            Assert.assertEquals("some field " + i, attrs.get(expectedAttr));

            attrName = attrName + ".deepEmbedded";
        }

        for (int m = 0; m < 0; m++) {
            AttributeDefinition expectedListAttr = new AttributeDefinition("embeddeds",
                    AttributeType.STRING, null);
            Assert.assertTrue(attrs.containsKey(expectedListAttr));
        }

    }

    @Test
    public void testSchemaValidationWithRecursiveDependencies() {
        Set<AttributeConstraint> constraints = new HashSet<AttributeConstraint>();
        constraints.add(AttributeConstraint.buildPrimaryKeyConstraint());

        Set<org.devspark.aws.lorm.schema.AttributeDefinition> attributes = new HashSet<org.devspark.aws.lorm.schema.AttributeDefinition>();

        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition("id",
                AttributeType.STRING, constraints));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "someRandomField", AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "embedded.someField", AttributeType.STRING, null));
        attributes.add(new org.devspark.aws.lorm.schema.AttributeDefinition(
                "embedded.deepEmbedded", AttributeType.STRING, null));

        EntityToItemMapperImpl<SampleEntity> mapper = new EntityToItemMapperImpl<SampleEntity>(
                SampleEntity.class);

        List<SchemaValidationError> positiveCaseValidationErrors = mapper
                .validateSchema(new EntitySchema("sampleentity", attributes, null));

        // TODO expect error of recursive dependency path
        Assert.assertEquals(1, positiveCaseValidationErrors.size());

        SchemaValidationError recursiveDependencyError = positiveCaseValidationErrors
                .get(0);
        Assert.assertNotNull(recursiveDependencyError);
        Assert.assertEquals(SchemaValidationErrorType.GENERAL,
                recursiveDependencyError.getErrorType());
        Assert.assertEquals("deepEmbedded", recursiveDependencyError.getIdentifier());
        Assert.assertEquals(SchemaValidationScope.ATTRIBUTE,
                recursiveDependencyError.getValidationScope());
        Assert.assertTrue(recursiveDependencyError.getMessage().contains("recursive"));

    }

    private SampleEmbeddable buildSampleEmbeddable(int levels) {
        SampleEmbeddable sampleEmbeddable = new SampleEmbeddable();
        sampleEmbeddable.setSomeField("some field " + levels);
        if (levels > 0) {
            sampleEmbeddable.setDeepEmbedded(buildSampleEmbeddable(--levels));
        }

        return sampleEmbeddable;
    }

}
