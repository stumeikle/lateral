package transgenic.lauterbrunnen.lateral.di;

import transgenic.lauterbrunnen.lateral.Lateral;

/**
 * Created by stumeikle on 15/05/16.
 */
public class BindBuilder<T> {

    private Class<T>   interfaceClass;
    private ApplicationCDI applicationCDI;

    public BindBuilder(ApplicationCDI applicationCDI, Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
        this.applicationCDI = applicationCDI;
    }

    public void to(T implementation) throws DIException {
        applicationCDI.registerImplementation(interfaceClass, implementation);
    }
    public void to(Class<? extends T> implementationClass) throws DIException {
        applicationCDI.registerImplementationClass(interfaceClass, implementationClass);
    }

    //and with context
    public void to(Class<? extends LateralDIContext> context, T implementation) {
        applicationCDI.registerImplementation(interfaceClass, context, implementation);
    }
    public void to(Class<? extends LateralDIContext> context, Class<? extends T> implementationClass) {
        applicationCDI.registerImplementationClass(interfaceClass,context, implementationClass);
    }

}
