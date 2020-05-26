package transgenic.lauterbrunnen.lateral.dicontext.plugins;

import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;

/**
 * Created by stumeikle on 28/07/19.
 */
@DIContext(MyContext2.class)
@DefaultImpl
public class MyContext2 implements LateralDIContext {
}
