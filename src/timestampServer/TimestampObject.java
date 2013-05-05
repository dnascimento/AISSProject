package timestampServer;

import java.io.Serializable;

public class TimestampObject
        implements Serializable {
    private static final long serialVersionUID = -4009171162105117498L;
    byte[] dataHash;
    long timestamp;
    byte[] signature;

    public TimestampObject(byte[] dataHash, long timestamp) {
        super();
        this.dataHash = dataHash;
        this.timestamp = timestamp;
    }

    public void setSignature(byte[] sign) {
        signature = sign;
    }

}
