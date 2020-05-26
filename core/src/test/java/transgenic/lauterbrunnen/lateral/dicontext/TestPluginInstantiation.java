package transgenic.lauterbrunnen.lateral.dicontext;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import transgenic.lauterbrunnen.lateral.Lateral;
import transgenic.lauterbrunnen.lateral.dicontext.plugins.AmazingPlugin;

/**
 * Created by stumeikle on 31/07/19.
 */
public class TestPluginInstantiation {

    @Test
    public void test() {

        Lateral.INSTANCE.initialiseDI("transgenic.lauterbrunnen.lateral.dicontext.plugins");
        BasicConfigurator.configure();

        Lateral.INSTANCE.initialisePlugins();

//        System.out.println("Output:" + AmazingPlugin.getOutput());
        boolean ok = false;
        if ("Hello from my context 1Hello from my context 2".equals(AmazingPlugin.getOutput())) ok = true;
        if ("Hello from my context 2Hello from my context 1".equals(AmazingPlugin.getOutput())) ok = true;
        assert(ok);
    }
}
