package transgenic.lauterbrunnen.lateral.entity.generator;

import org.junit.Test;

/**
 * Created by stumeikle on 01/07/20.
 */
public class TestCassandraFirstTypeSwap {

    @Test
    public void testFirstTypeSwap() {
        CassandraFirstTypeSwap cassandraFirstTypeSwap = new CassandraFirstTypeSwap();
        cassandraFirstTypeSwap.init("id");
        String result = cassandraFirstTypeSwap.runFirstTypeSwap();

        assert(("Long updateId;\n" +
                "java.util.UUID id;\n" +
                "Integer number;\n" +
                "Double fraction;\n" +
                "Float percentage;\n" +
                "java.math.BigDecimal decimal;\n" +
                "java.lang.String description;\n" +
                "java.util.Set<java.lang.String> reviews;\n" +
                "java.util.Set<java.lang.Integer> stars;\n" +
                "java.util.Map<java.util.UUID,java.lang.String> lookup;\n" +
                "java.util.Set<java.lang.Double> proportions;\n" +
                "java.util.Map<java.util.UUID,java.util.Set<java.lang.String>> contacts;\n").equals(result));
    }

    @Test
    public void testSecondTypeSwap() {
        CassandraFirstTypeSwap cassandraFirstTypeSwap = new CassandraFirstTypeSwap();
        cassandraFirstTypeSwap.init("id");
        String result = cassandraFirstTypeSwap.runSecondTypeSwap();

        assert(("bigint updateId;\n" +
                "uuid id;\n" +
                "int number;\n" +
                "double fraction;\n" +
                "float percentage;\n" +
                "decimal decimal;\n" +
                "text description;\n" +
                "set<text> reviews;\n" +
                "set<int> stars;\n" +
                "map<uuid,text> lookup;\n" +
                "set<double> proportions;\n" +
                "map<uuid,set<text>> contacts;\n").equals(result));
    }

    @Test
    public void testTableDescription() {
        CassandraFirstTypeSwap cassandraFirstTypeSwap = new CassandraFirstTypeSwap();
        cassandraFirstTypeSwap.init("id");
        String result = cassandraFirstTypeSwap.createTableDescription();

//        System.out.println(result);
        assert(("(UPDATE_ID bigint, ID uuid, NUMBER int, FRACTION double, PERCENTAGE float, DECIMAL decimal, DESCRIPTION text, REVIEWS set<text>, STARS set<int>, LOOKUP map<uuid,text>, PROPORTIONS set<double>, CONTACTS map<uuid,set<text>>, PRIMARY KEY (ID))")
        .equals(result));
    }

    @Test
    public void testCreateGettersAndSetters() {
        CassandraFirstTypeSwap cassandraFirstTypeSwap = new CassandraFirstTypeSwap();
        cassandraFirstTypeSwap.init("id");
        String result = cassandraFirstTypeSwap.createGettersAndSetters();

        assert(("@Column(name=\"UPDATE_ID\")\n" +
                "    public Long getUpdateId() { return this.updateId;}\n" +
                "\n" +
                "    public void setUpdateId( Long updateId) { this.updateId = updateId;}\n" +
                "\n" +
                "    @PartitionKey\n" +
                "    @Column(name=\"ID\")\n" +
                "    public java.util.UUID getId() { return this.id;}\n" +
                "\n" +
                "    public void setId( java.util.UUID id) { this.id = id;}\n" +
                "\n" +
                "    @Column(name=\"NUMBER\")\n" +
                "    public Integer getNumber() { return this.number;}\n" +
                "\n" +
                "    public void setNumber( Integer number) { this.number = number;}\n" +
                "\n" +
                "    @Column(name=\"FRACTION\")\n" +
                "    public Double getFraction() { return this.fraction;}\n" +
                "\n" +
                "    public void setFraction( Double fraction) { this.fraction = fraction;}\n" +
                "\n" +
                "    @Column(name=\"PERCENTAGE\")\n" +
                "    public Float getPercentage() { return this.percentage;}\n" +
                "\n" +
                "    public void setPercentage( Float percentage) { this.percentage = percentage;}\n" +
                "\n" +
                "    @Column(name=\"DECIMAL\")\n" +
                "    public java.math.BigDecimal getDecimal() { return this.decimal;}\n" +
                "\n" +
                "    public void setDecimal( java.math.BigDecimal decimal) { this.decimal = decimal;}\n" +
                "\n" +
                "    @Column(name=\"DESCRIPTION\")\n" +
                "    public java.lang.String getDescription() { return this.description;}\n" +
                "\n" +
                "    public void setDescription( java.lang.String description) { this.description = description;}\n" +
                "\n" +
                "    @Column(name=\"REVIEWS\")\n" +
                "    public java.util.Set<java.lang.String> getReviews() { return this.reviews;}\n" +
                "\n" +
                "    public void setReviews( java.util.Set<java.lang.String> reviews) { this.reviews = reviews;}\n" +
                "\n" +
                "    @Column(name=\"STARS\")\n" +
                "    public java.util.Set<java.lang.Integer> getStars() { return this.stars;}\n" +
                "\n" +
                "    public void setStars( java.util.Set<java.lang.Integer> stars) { this.stars = stars;}\n" +
                "\n" +
                "    @Column(name=\"LOOKUP\")\n" +
                "    public java.util.Map<java.util.UUID,java.lang.String> getLookup() { return this.lookup;}\n" +
                "\n" +
                "    public void setLookup( java.util.Map<java.util.UUID,java.lang.String> lookup) { this.lookup = lookup;}\n" +
                "\n" +
                "    @Column(name=\"PROPORTIONS\")\n" +
                "    public java.util.Set<java.lang.Double> getProportions() { return this.proportions;}\n" +
                "\n" +
                "    public void setProportions( java.util.Set<java.lang.Double> proportions) { this.proportions = proportions;}\n" +
                "\n" +
                "    @Column(name=\"CONTACTS\")\n" +
                "    public java.util.Map<java.util.UUID,java.util.Set<java.lang.String>> getContacts() { return this.contacts;}\n" +
                "\n" +
                "    public void setContacts( java.util.Map<java.util.UUID,java.util.Set<java.lang.String>> contacts) { this.contacts = contacts;}").equals(result.trim()));
    }

    @Test
    public void transformImplToEntity() {
        CassandraFirstTypeSwap cassandraFirstTypeSwap = new CassandraFirstTypeSwap();
        cassandraFirstTypeSwap.init("id");

        String result= cassandraFirstTypeSwap.createTransform2EntityFromImplMethod("CassandraTestObjectEntity", CassandraTestObjectPrototype.class );
        System.out.println(result);

        String reverse= cassandraFirstTypeSwap.createTransform2ImplFromEntityMethod("CassandraTestObjectEntity", CassandraTestObjectPrototype.class);
        System.out.println(reverse);
    }
}
