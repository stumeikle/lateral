package transgenic.lauterbrunnen.lateral.example.multidomain.shipping.logic;

import transgenic.lauterbrunnen.lateral.domain.Factory;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.Address;
import transgenic.lauterbrunnen.lateral.example.multidomain.shipping.generated.AddressImpl;

/**
 * Created by stumeikle on 24/02/20.
 */
public class MockedFactory implements Factory {
    @Override
    public <T> T create(Class<T> aClass) {

        if (aClass==Address.class) {
            return (T) new AddressImpl();
        }

        return null;
    }
}
