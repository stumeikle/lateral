package transgenic.lauterbrunnen.lateral.dicontext.plugins;

import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.DefaultImpl;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;

/**
 * Created by stumeikle on 31/07/19.
 */
@DIContext(MyContext3.class)
@DefaultImpl
public class MyContext3 implements LateralDIContext {
}
