package transgenic.lauterbrunnen.lateral.domain;

/**
 * Created by stumeikle on 13/05/16.
 */

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Stuart.meikle on 11/05/2016.
 */
public class UniqueId implements Serializable{
    //Type 1 (time based) UUID. See https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html
    //plus reorder to make more sequential. ref https://www.percona.com/blog/2014/12/19/store-uuid-optimized-way/
    private static final NoArgGenerator nag = Generators.timeBasedGenerator();

    private byte[] value;

    public static UniqueId generate() {

        UUID uuid = reorder(nag.generate());
        UniqueId retval = new UniqueId();
        retval.setValue(toByteArray(uuid));

        //convert to byte
        return retval;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    //we could wrap UUID here but I don't want to
    public UUID convertToJavaUUID( ) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (value[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (value[i] & 0xff);
        UUID result = new UUID(msb, lsb);
        return result;
    }

    //You can create from a uuid but you should only do this if the uuid was created from a unique id in the first place
    //WARNING . else the necessary bit reordering will not occur.
    public static UniqueId revertUuidToUniqueId(UUID uuid) {
        UniqueId reversed = new UniqueId();
        reversed.setValue(toByteArray(uuid));
        return reversed;
    }

    //taken from https://gist.github.com/lifecoder/1153724
    private static byte[] toByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits()); // order is important here!
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID reorder(UUID uuid) {

        //we can re-order using the longs directly
        long msb = uuid.getMostSignificantBits();
        long newmsb = ((msb&0xFFFFFFFF00000000L)>>>(32L)) |  //32 bits = 8 Hex chars
                ((msb&0xFFFF0000L)<<16L) |
                ((msb&0xF000L)<<48L) |
                ((msb&0xFFFL)<<48L);


        return new UUID(newmsb, uuid.getLeastSignificantBits());
    }

    //if you need to use them...
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof UniqueId)) return false;

        UniqueId uid = (UniqueId)other;
        if (!Arrays.equals(value, uid.value))
            return false;
        return true;
    }

    public String toString() {
        ByteBuffer bb = ByteBuffer.wrap(value);
        long msb= bb.getLong();
        long lsb= bb.getLong();
        UUID uuid = new UUID(msb,lsb);
        return uuid.toString();
    }

    //incoming string is from a unique id. ie it is already reordered
    public static UniqueId fromString(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        UniqueId retval = new UniqueId();
        retval.setValue(toByteArray(uuid));
        return retval;
    }

    public static byte[] convertToByteArray(UniqueId uniqueId) {
        return uniqueId.getValue();
    }
    public static UniqueId createFromByteArray(byte[] byteArray) {
        UniqueId retval = new UniqueId();
        retval.setValue(byteArray);
        return retval;
    }
}
