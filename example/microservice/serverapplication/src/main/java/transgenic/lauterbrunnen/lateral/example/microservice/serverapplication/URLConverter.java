package transgenic.lauterbrunnen.lateral.example.microservice.serverapplication;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by stumeikle on 17/11/16.
 */
public class URLConverter {

    public static String convertToString(URL url) {
        return url.toString();
    }

    public static URL createFromString(String surl) {
        try {
            return new URL(surl);
        } catch (MalformedURLException me) {
            return null;
        }
    }
}
