package transgenic.lauterbrunnen.lateral.dicontext;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.di.ApplicationCDI;
import transgenic.lauterbrunnen.lateral.di.DIException;
import transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls.*;

import java.util.Properties;

import static transgenic.lauterbrunnen.lateral.Lateral.inject;
import static transgenic.lauterbrunnen.lateral.Lateral.registerImplementationClass;

/**
 * Created by stumeikle on 27/07/19.
 */
public class TestApplicationCDI {

    @Test
    public void test() {

        boolean exceptionThrown  = false;

        try {
            ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.onecontextmultipledefaultimpls",null);
        } catch (DIException e) {
//            e.printStackTrace();
            exceptionThrown = true;
        }

        assert(exceptionThrown);
    }

    @Test
    public void test2() {

        boolean exceptionThrown  = false;

        try {
            ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.onecontextmultipledefaultimpls2",null);
        } catch (DIException e) {
//            e.printStackTrace();
            exceptionThrown = true;
        }

        assert(exceptionThrown);
    }

    @Test
    public void test2point5() {

        boolean exceptionThrown  = false;

        try {
            ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.onecontextmultipledefaultimpls3",null);
        } catch (DIException e) {
//            e.printStackTrace();
            exceptionThrown = true;
        }

        assert(exceptionThrown);
    }

    @Test
    public void test3() {

        boolean exceptionThrown  = false;

        try {
            ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls",null);
        } catch (DIException e) {
//            e.printStackTrace();
            exceptionThrown = true;
        }

        assert(!exceptionThrown);
    }

    @Test
    public void test4() {

        Properties  properties = new Properties();
        properties.setProperty("di.class.for.MyInterface", InterfaceImpl2.class.getName());
        properties.setProperty("di.class.for.MyInterface.context.MyContext", InterfaceImpl1.class.getName());

        try {
            ApplicationCDI applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls",properties);
        } catch (DIException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5() {

        ApplicationCDI applicationCDI=null;
        try {
            applicationCDI = new ApplicationCDI("transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls",null);

            //this should barf as there are 2 contexts defining default impls
            MyInterface mi = applicationCDI.getImplementation(MyInterface.class);
            assert(false); // shouldn't get here

        } catch (DIException e) {
            String expected = "Unable to get implementation for class interface transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls.MyInterface without specified context as multiple contexts implement it.";
            assert(e.getMessage().equals(expected));
        }

        try {
            MyInterface mi = applicationCDI.getImplementation(MyInterface.class, MyContext.class);
            System.out.println(mi.getMessage());

        } catch (DIException e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void test6() {
        Lateral.INSTANCE.initialiseDI("transgenic.lauterbrunnen.lateral.dicontext.twocontextstwodefaultimpls");

        BasicConfigurator.configure();

        boolean ok = true;
        MyInterface myc = inject(MyInterface.class);
        if (myc!=null) ok =false;
        myc = inject(MyInterface.class, MyContext.class);
        System.out.println(myc.getMessage());
        assert(ok);

        //Logic
        //(1) should throw;
        ok = registerImplementationClass(MyInterface.class, SecondImpl.class);
        assert(!ok);

        //(2) should be ok
        registerImplementationClass(MyInterface.class, MyOtherContext.class, SecondImpl.class);

        //(3) now inject
        myc = inject(MyInterface.class, MyOtherContext.class);
        System.out.println(myc.getMessage());
        assert("a new hope".equals(myc.getMessage()));
    }

}
