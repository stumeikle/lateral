package transgenic.lauterbrunnen.lateral.domain;

/**
 * Created by stumeikle on 04/06/16.
 */
//@DI
public interface FactoryContract {

    <T> T create(Class<T> clazz);
    CRUDRepository getRepositoryForClass(Class clazz);
}
