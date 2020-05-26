package transgenic.lauterbrunnen.lateral.domain;


import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 30/05/16.
 * This needs to become a proxy itself :-)
 *
 * to allow users to change the implementation
 */
public class Factory {

    private static final FactoryContract factoryViaDI = inject(FactoryContract.class);

    public static <T> T create(Class<T> clazz) {
        return factoryViaDI.create(clazz);
    }

    public static CRUDRepository getRepositoryForClass(Class clazz) {
        return factoryViaDI.getRepositoryForClass(clazz);
    }

}
