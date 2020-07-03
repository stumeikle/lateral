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

    public static String getString(Properties properties, String key, String defaultValue) {
        if (properties !=null) {
            String value = (String) properties.get(key);

            if (value!=null) {
                return value;
            }
        }

        return defaultValue;
    }

    public static int getInteger(Properties properties, String key, int defaultValue) {
        if (properties !=null) {
            String value = (String) properties.get(key);

            if (value!=null) {
                try {
                    int intValue = Integer.parseInt(value.trim());
                    return intValue;
                } catch(Exception e) {}
            }
        }

        return defaultValue;
    }
}
