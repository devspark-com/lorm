package org.devspark.aws.lorm.mapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.devspark.aws.lorm.DummyEntityManager;
import org.devspark.aws.lorm.mapping.ItemToEntityMapperImpl;
import org.devspark.aws.lorm.schema.AttributeConstraint;
import org.devspark.aws.lorm.schema.AttributeDefinition;
import org.devspark.aws.lorm.schema.AttributeType;
import org.devspark.aws.lorm.test.model.Category;
import org.devspark.aws.lorm.test.model.Expense;
import org.devspark.aws.lorm.test.model.ExpenseType;
import org.devspark.aws.lorm.test.model.Merchant;
import org.devspark.aws.lorm.test.model.Reporter;
import org.devspark.aws.lorm.test.model.embedded.SampleEntity;
import org.junit.Assert;
import org.junit.Test;

public class ItemToEntityMapperImplTest {

    @Test
    public void testMap() {
	Category category = new Category();
	category.setId("categoryid");
	category.setDescription("category description");
	DummyEntityManager.get().getRepository(Category.class).save(category);

	Merchant merchant = new Merchant();
	merchant.setId("merchantid");
	merchant.setName("merchant name");
	DummyEntityManager.get().getRepository(Merchant.class).save(merchant);

	Reporter reporter = new Reporter();
	reporter.setId("reporterid");
	reporter.setName("reporter name");
	DummyEntityManager.get().getRepository(Reporter.class).save(reporter);

	Map<AttributeDefinition, Object> attributes = new HashMap<AttributeDefinition, Object>();
	Set<AttributeConstraint> pkConstraints = new HashSet<AttributeConstraint>();
	pkConstraints.add(AttributeConstraint.buildPrimaryKeyConstraint());
	attributes.put(new AttributeDefinition("id", AttributeType.STRING, pkConstraints),
		"expenseid");
	attributes.put(new AttributeDefinition("description", AttributeType.STRING, null),
		"expense description");
	attributes.put(new AttributeDefinition("category.id", AttributeType.STRING, null),
		"categoryid");
	attributes.put(new AttributeDefinition("merchant.id", AttributeType.STRING, null),
		"merchantid");
	attributes.put(new AttributeDefinition("reporter.id", AttributeType.STRING, null),
		"reporterid");
	attributes.put(new AttributeDefinition("date", AttributeType.NUMBER, null), 999999);
	attributes.put(new AttributeDefinition("attachment.location", AttributeType.STRING, null),
		"attachment location");
	attributes.put(
		new AttributeDefinition("attachment.description", AttributeType.STRING, null),
		"attachment description");
	attributes.put(new AttributeDefinition("expenseType", AttributeType.STRING, null),
		"REIMBURSABLE");

	ItemToEntityMapperImpl<Expense> expenseMapper = new ItemToEntityMapperImpl<Expense>(
		Expense.class, DummyEntityManager.get());
	Expense expense = expenseMapper.map(attributes);

	Assert.assertEquals("expenseid", expense.getId());
	Assert.assertEquals("merchantid", expense.getMerchant().getId());
	Assert.assertEquals("categoryid", expense.getCategory().getId());
	Assert.assertEquals("reporterid", expense.getReporter().getId());
	Assert.assertEquals(999999, expense.getDate().getTime());
	Assert.assertNotNull(expense.getAttachment());
	Assert.assertEquals("attachment location", expense.getAttachment().getLocation());
	Assert.assertEquals("attachment description", expense.getAttachment().getDescription());
	Assert.assertEquals(ExpenseType.REIMBURSABLE, expense.getExpenseType());
    }

    @Test
    public void testDeepEmbedded() {
	Map<AttributeDefinition, Object> attributes = new HashMap<AttributeDefinition, Object>();
	Set<AttributeConstraint> pkConstraints = new HashSet<AttributeConstraint>();
	pkConstraints.add(AttributeConstraint.buildPrimaryKeyConstraint());
	attributes.put(new AttributeDefinition("id", AttributeType.STRING, pkConstraints),
		"sampleentityid");
	attributes.put(new AttributeDefinition("someRandomField", AttributeType.STRING, null),
		"sample entity description");
	attributes.put(new AttributeDefinition("embedded.someField", AttributeType.STRING, null),
		"some field 1");
	attributes.put(new AttributeDefinition("embedded.deepEmbedded.someField",
		AttributeType.STRING, null), "some field 2");
	attributes.put(new AttributeDefinition("embedded.deepEmbedded.deepEmbedded.someField",
		AttributeType.STRING, null), "some field 3");

	ItemToEntityMapperImpl<SampleEntity> sampleEntityMapper = new ItemToEntityMapperImpl<SampleEntity>(
		SampleEntity.class, DummyEntityManager.get());

	SampleEntity sampleEntity = sampleEntityMapper.map(attributes);
	Assert.assertEquals("sampleentityid", sampleEntity.getId());
	Assert.assertEquals("sample entity description", sampleEntity.getSomeRandomField());

	Assert.assertNotNull(sampleEntity.getEmbedded());
	Assert.assertEquals("some field 1", sampleEntity.getEmbedded().getSomeField());

	Assert.assertNotNull(sampleEntity.getEmbedded().getDeepEmbedded());
	Assert.assertEquals("some field 2",
		sampleEntity.getEmbedded().getDeepEmbedded().getSomeField());

	Assert.assertNotNull(sampleEntity.getEmbedded().getDeepEmbedded().getDeepEmbedded());
	Assert.assertEquals("some field 3",
		sampleEntity.getEmbedded().getDeepEmbedded().getDeepEmbedded().getSomeField());

    }
}
