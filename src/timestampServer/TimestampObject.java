package timestampServer;

import java.io.Serializable;
import java.util.Date;

public class TimestampObject
        implements Serializable {
    byte[] dataHash;
    Date signatureDate;
    byte[] signature;

    public TimestampObject(byte[] dataHash, Date signatureDate, byte[] signature) {
        super();
        this.dataHash = dataHash;
        this.signatureDate = signatureDate;
        this.signature = signature;
    }

}
