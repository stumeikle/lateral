package transgenic.lauterbrunnen.lateral.domain;

import transgenic.lauterbrunnen.lateral.admin.Admin;

import java.util.*;

/**
 * Created by stumeikle on 31/05/16.
 *
 */
public class Repository {

    private static EntityTraversalFunction traverseAndPersist = new EntityTraversalFunction() {
        @Override
        public void action(EntityImpl entity, Object subentity, Collection<EntityImpl> persistCollection) throws PersistenceException {

            if (subentity instanceof EntityImpl) {
                EntityImpl impl = (EntityImpl)subentity;
                //if (impl.getRepositoryId()==null) throw new PersistenceException(); able to store with empty ids now
                if (!persistCollection.contains(impl)) {
                    persistCollection.add(impl);

                    //then traverse
                    if (!entity.equals(impl)) {
                        impl.traverse( traverseAndPersist, persistCollection);
                    }
                }
            }

            if (subentity instanceof List) {
                List    list = (List)subentity;
                for(int i=0;i<list.size();i++) {
                    Object o = list.get(i);
                    action(entity, o, persistCollection);

                    //need to change the list to switch o with a reference to o
                    if (o instanceof EntityImpl) {
                        EntityReference reference = ((EntityImpl)o).getReference();
                        list.set(i,reference);
                    }
                }
            }

            if (subentity instanceof Map) {
                //check keys and values
                Map map = (Map) subentity;
                Map changes = new HashMap<>();
                for(Object key : map.keySet() ) {
                    action(entity, key, persistCollection);

                    Object value = map.get(key);
                    action(entity, value, persistCollection);

                    if ((key instanceof EntityImpl) || (value instanceof EntityImpl)) {
                        changes.put(key, value);
                    }
                }

                //now change the impls to references
                for(Object key: changes.keySet()) {
                    Object value = map.get(key);
                    map.remove(key);
                    if (key instanceof EntityImpl) {
                        EntityImpl ei = (EntityImpl) key;
                        key = ei.getReference();
                    }
                    if (value instanceof  EntityImpl) {
                        EntityImpl ei = (EntityImpl) value;
                        value = ei.getReference();
                    }
                    map.put(key,value);
                }
            }

        }
    };

    private static EntityTraversalFunction traverseAndUpdate = new EntityTraversalFunction() {
        @Override
        public void action(EntityImpl entity, Object subentity, Collection<EntityImpl> updateCollection) throws PersistenceException {
            //in this case subentity could be an impl or a reference
            //if it is an impl it must have already been persisted
            EntityImpl      impl2Update=null;
            if (subentity instanceof EntityReference) {
                //ok.
                //if the proxee is unset then do not descend
                impl2Update = (EntityImpl)((EntityReference) subentity).getProxee();
                if (impl2Update==null) return;
            } else {
                if (subentity instanceof EntityImpl) {
                    impl2Update = (EntityImpl) subentity;
                    if (Repository.retrieve(subentity.getClass(), impl2Update.getRepositoryId())==null) {
                        throw new PersistenceException(); //trying to update an object which is not yet in the cache
                    }

//                    if (!impl2Update.hasBeenPersisted()) {
//                        throw new PersistenceException(); //trying to update an object which is not yet in the db
                        //20161202 this no longer makes sense to me
//                    }
                }
            }

            //impossible anyway
            //if (impl2Update.getUniqueIdentifier()==null) throw new PersistenceException();
            if (impl2Update!=null) {
                if (!updateCollection.contains(impl2Update)) {
                    updateCollection.add(impl2Update);

                    //then traverse
                    if (!entity.equals(impl2Update)) {
                        impl2Update.traverse( traverseAndUpdate, updateCollection);
                    }
                }
            }

            if (subentity instanceof List) {
                List    list = (List)subentity;
                for(int i=0;i<list.size();i++) {
                    Object o = list.get(i);
                    action(entity, o, updateCollection);

                    //need to change the list to switch o with a reference to o
                    if (o instanceof EntityImpl) {
                        EntityReference reference = ((EntityImpl)o).getReference();
                        list.set(i,reference);
                    }
                }
            }

            if (subentity instanceof Map) {
                //check keys and values
                Map map = (Map) subentity;
                Map changes = new HashMap<>();
                for(Object key : map.keySet() ) {
                    action(entity, key, updateCollection);

                    Object value = map.get(key);
                    action(entity, value, updateCollection);

                    if ((key instanceof EntityImpl) || (value instanceof EntityImpl)) {
                        changes.put(key, value);
                    }
                }

                //now change the impls to references
                for(Object key: changes.keySet()) {
                    Object value = map.get(key);
                    map.remove(key);
                    if (key instanceof EntityImpl) {
                        EntityImpl ei = (EntityImpl) key;
                        key = ei.getReference();
                    }
                    if (value instanceof  EntityImpl) {
                        EntityImpl ei = (EntityImpl) value;
                        value = ei.getReference();
                    }
                    map.put(key,value);
                }
            }

        }
    };

    //Previous comments:
    //TODO we could generate ids here but is this the right place?
    //TODO could also use a queue instead of a set to preserve traversal order if needed
    //TODO we could split the repository calls here into blocks. but we'd not then be able to
    public static void persist(Object entity) throws PersistenceException {

        if (!(entity instanceof EntityImpl)) {
            throw new PersistenceException();
        }

        EntityImpl entityImpl = (EntityImpl)entity;
        Set<EntityImpl> entities = new HashSet<>();
        entityImpl.traverse( traverseAndPersist, entities );
        Factory.getRepositoryForClass(entityImpl.getClass()).persistAll(entities);

    }

    public static void update(Object entity) throws PersistenceException {

        if (!((entity instanceof EntityImpl) || (entity instanceof EntityReference))) {
            throw new PersistenceException();
        }

        //how to do this?
        //could either get all the objects and pass to the cache or only the changed ones
        //i'll leave the delta computation to the cache

        EntityImpl entityImpl = null;
        if (entity instanceof EntityImpl) {
            entityImpl = (EntityImpl) entity;
        } else {
            if (entity instanceof  EntityReference) {
                entityImpl = (EntityImpl) ((EntityReference)entity).getProxee();
            }
        }
        if (entity == null) throw new PersistenceException();

        Set<EntityImpl> entities = new HashSet<>();
        entityImpl.traverse( traverseAndUpdate, entities );
        Factory.getRepositoryForClass(entityImpl.getClass()).updateAll(entities);
    }

    public static <T> T retrieve(Class<? extends T> clazz, Object id) {

        CRUDRepository repo = Factory.getRepositoryForClass(clazz);
        return (T) repo.retrieve(id);
    }

    //Delete ...
    public static <T> void delete(Class<? extends T> clazz, Object id){
        //this next could also live in the individual caches. consider
        CRUDRepository repo = Factory.getRepositoryForClass(clazz);
        repo.delete(id);
    }

}
