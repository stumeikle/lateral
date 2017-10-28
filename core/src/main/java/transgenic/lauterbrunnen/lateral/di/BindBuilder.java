package transgenic.lauterbrunnen.lateral.di;

/**
 * Created by stumeikle on 15/05/16.
 */
public class BindBuilder<T> {

    private Class<T>   interfaceClass;

    public BindBuilder(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void to(T implementation) {
        ApplicationDI.registerImplementation(interfaceClass, implementation);
    }
    public void to(Class<? extends T> implementationClass) {
        ApplicationDI.registerImplementationClass(interfaceClass, implementationClass);
    }

}
