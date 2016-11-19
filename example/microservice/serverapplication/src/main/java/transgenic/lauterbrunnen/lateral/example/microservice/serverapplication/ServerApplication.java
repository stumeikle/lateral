package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.domain.Repository;
import transgenic.lauterbrunnen.lateral.domain.UniqueId;
import transgenic.lauterbrunnen.lateral.example.microservice.libdomain.generated.Album;


/**
 * Created by stumeikle on 03/11/16.
 *
 * TODO:
 * Ensure unknown types are correctly persisted to the db
 *
 * the rest concept wrt References needs to be rethought. why return whole object graphs on
 * a single retrieve,that makes no sense. should be using references and give the user the option
 * to descend or pull all. i suppose in general smaller bits is better
 *
 * could be clever about it. could say if the object is a reference set the id else set all
 * the fields. that would allow us to defer object structure to the cache layer and prevent us
 * duplicating the logic
 *
 * other TODO:
 * test this end to end
 * think about search engine integration. solr etc
 * think about using orm.xml like here: http://www.bigdev.org/2012/01/overwriting-jpa-annotations-with-orm-xml/
 * to support multiple db types without regenerating all the entities
 * (although you can right now support with build type flags)
 * admin bus
 * kryo / fst
 *
 * BUGS:
 *
 * (1) rest endpoints expect strings for ids and don't correctly convert to the underlying type
 * eg double, UniqueId
 * [done . looks like jersey can handle it magically]
 *
 * (2) Repository find uses the internal representationId, eg unique id
 * but the hazelcast mapstore should convert this to the entity type eg byte[]
 * [done]
 *
 * (3) need to put NULL checks into the REst Pojos. eg setAlbum( Album.createFromAlbum( getalbum() ) will fail if null
 * [done]
 *
 * (4) creating new entries does not fill out the repository id
 * [dpne]
 *
 * (5) if i create a new entry, i need the unique id back
 * Think this is a generally needed thing. ids -could- be assigned by the db we should return them
 * if there's a whole set of objects created we'll return the top most
 *
 * -> lots of complications here. need to also consider what happens with sequences.
 * if the entity has no id then corresponding hashcodes get a bit f-d up
 * otherwise i think we can accommodate fairly straightforwardly
 * see null checks in repository.java
 * have to think about this. the ids are used for hashcode and equals so maybe not so easy to use sequences
 * we'd have to persist first and traverse second
 * this works for eclipse ddl
 *     @GeneratedValue(strategy=GenerationType.AUTO)
 @SequenceGenerator(name="my_seq",sequenceName="MY_SEQ", allocationSize=1)
 * could possibly use custom generator see http://stackoverflow.com/questions/12742826/how-do-i-know-id-before-saving-object-in-jpa
  * might be ok if i set hashcode to -1 for these items and equals to always be false. need to consider creation in
  * detail
 *
 * (6) ensure x tables use the varchar for bit for foreign keys
 * [Done]
 *
 * (7) i'm not convinced structures like LIst<List<domain_obj>> will be correctly represented at the entity and
 * database. Indeed the whole entity generation might need to be recursive
 * [done for now]
 */
public class ServerApplication {

    private static final Log LOG = LogFactory.getLog(ServerApplication.class);

    public static void main(String[] args) {

        BasicConfigurator.configure();
        Lateral.INSTANCE.initialise();

        //UniqueId uuid = UniqueId.fromString("11e6adbe-eab2-3e37-b74d-296553578ef7"); //ok
        UniqueId uuid = UniqueId.fromString("11e6adbf-87c7-b651-99af-d5b572af5982"); //ok
//11e6adbf-87c7-b651-99af-d5b572af5982

        Album album = Repository.retrieve(Album.class, uuid);
        System.out.println("Result=" + album);

    }
}
