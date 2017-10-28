package transgenic.lauterbrunnen.lateral.di;

import org.junit.Test;

import static transgenic.lauterbrunnen.lateral.di.ApplicationDI.inject;

/**
 * Created by stumeikle on 15/05/16.
 */
public class TestBind {


    @Test
    public void testBind() {

        //pure foppery
        ApplicationDI.bind( TestInterface.class).to( new TestImpl());

        //This is our one line inject

        TestInterface i = inject(TestInterface.class);
        i.logMessage();


    }
}
