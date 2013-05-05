package timestampServer;

import java.io.Serializable;
import java.util.Date;

public class TimestampObject
        implements Serializable {
	private static final long serialVersionUID = -4009171162105117498L;
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
