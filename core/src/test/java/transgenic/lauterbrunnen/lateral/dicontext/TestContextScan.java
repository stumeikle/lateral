package transgenic.lauterbrunnen.lateral.dicontext;

import org.junit.Test;
import transgenic.lauterbrunnen.lateral.di.DIContext;
import transgenic.lauterbrunnen.lateral.di.LateralDIContext;
import transgenic.lauterbrunnen.lateral.plugin.AnnotationScanner;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Created by stumeikle on 25/07/19.
 */
public class TestContextScan {

    @Test
    public void test1() {

        AnnotationScanner annotationScanner = new AnnotationScanner();
        annotationScanner.scan("transgenic.lauterbrunnen.lateral.dicontext",".ejb.", "transgenic.lauterbrunnen" );
        Set<Class> contextClasses = annotationScanner.get(DIContext.class);

        StringBuilder sb = new StringBuilder();
        for(Class clazz : contextClasses) {
            String packageCheck = clazz.getName();
            packageCheck = packageCheck.replace("transgenic.lauterbrunnen.lateral.dicontext.","");
            if (packageCheck.contains(".")) continue;

            for(Annotation note: clazz.getAnnotations()) {
                if (note.annotationType().getName().equals(DIContext.class.getName())) {
                    DIContext   diContext = (DIContext) note;
                    sb.append(diContext.value());
                }
            }
        }

//        System.out.println("SB=" + sb.toString());
        boolean ok = true;
        if (!sb.toString().contains("class transgenic.lauterbrunnen.lateral.dicontext.DomainContext")) ok=false;
        if (!sb.toString().contains("class transgenic.lauterbrunnen.lateral.dicontext.ServiceDiscoveryContext")) ok = false;
        assert(ok);
    }

    /*
        ApplicationCDI.initialise("transgenic.lauterbrunnen.lateral.dicontext");

        CommonInterface     ci = inject(CommonInterface.class); // should fail
        CommonInterface     ci = inject(CommonInterface.class, DomainContext.class); // ok
        //check the message
        CommonInterface     ci = inject(CommonInterface.class, ServiceDiscoveryContext.class); // ok
        //check the message

        Factory    factory = inject(Factory.class); // should be ok. only one context impl so use that

     */
}
