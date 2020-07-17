package transgenic.lauterbrunnen.lateral.domain;

import org.junit.Test;

import java.util.UUID;

/**
 * Created by stumeikle on 01/07/20.
 */
public class TestUniqueIdToUUID {

    @Test
    public void test() {

        UniqueId    uniqueId = UniqueId.generate();

        System.out.println("Unique id is " + uniqueId);

        String original = uniqueId.toString();

        UUID uuid = UniqueId.convertToJavaUUID(uniqueId);

        System.out.println("Corresponding uuid is " + uuid);

        UniqueId reversed = UniqueId.revertUuidToUniqueId(uuid);

        System.out.println("And reversed uuid back to unique id gives " + reversed);

        assert(reversed.toString().equals(original));
    }
}
