package transgenic.lauterbrunnen.lateral.di;

import org.junit.Test;

/**
 * Created by stumeikle on 15/05/16.
 */
public class TestBind {


    @Test
    public void testBind() throws DIException {

        //pure foppery
        ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.di", null);
        applicationCDI.bind( TestInterface.class).to( new TestImpl());

        //This is our one line inject
        TestInterface i = applicationCDI.inject(TestInterface.class);
        i.logMessage();
    }
}
