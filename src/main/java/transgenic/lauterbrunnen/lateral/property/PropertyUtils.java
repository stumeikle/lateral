package transgenic.lauterbrunnen.lateral.property;

import java.util.Optional;
import java.util.Properties;

/**
 * Created by stumeikle on 14/05/16.
 */
public class PropertyUtils {

    public static Optional<Boolean> getBoolean(Properties properties, String key) {
        if (properties !=null) {
            String value = (String) properties.get(key);

            if (value!=null) {
                boolean b = Boolean.valueOf(value);
                return Optional.of(b);
            }
        }

        return Optional.empty();
    }
}
