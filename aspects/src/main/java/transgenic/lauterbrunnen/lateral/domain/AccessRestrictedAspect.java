package transgenic.lauterbrunnen.lateral.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by stumeikle on 02/10/20.
 */
@Aspect
public class AccessRestrictedAspect {

    private static final Log LOG = LogFactory.getLog(AccessRestrictedAspect.class);

    @Pointcut("@annotation(accessRestricted)")
    public void callAt(AccessRestricted accessRestricted) {}

    @Around("callAt(accessRestricted)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, AccessRestricted accessRestricted) throws Throwable {

        LOG.trace("Around accessRestricted called");

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length>3) {
            String accessAttemptedFrom = elements[3].getClassName();
            String prefix = accessRestricted.callerPackagePrefix();
            if (prefix.isEmpty()){
                prefix=proceedingJoinPoint.getSignature().getDeclaringTypeName();
                int lastDotIndex=prefix.lastIndexOf('.');
                prefix=prefix.substring(0,lastDotIndex+1);
            }

            if (!accessAttemptedFrom.startsWith(prefix)) {
                throw new AccessViolationException(accessAttemptedFrom + " attempting to access " + proceedingJoinPoint.getSignature());
            }
        }

        return proceedingJoinPoint.proceed();
    }
}
