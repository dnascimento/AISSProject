package aiss;

import java.io.Serializable;
import java.security.cert.Certificate;

/**
 * DTO for security data
 */
public class AissMime
        implements Serializable {
    private static final long serialVersionUID = -4009171162105117499L;

    // Timestamp Sign
    public byte[] timestampSign;
    // DataSign
    public byte[] datasign;
    // Datacipher
    public byte[] datacipher;
    // EmailTextLenght
    public int emailTextLenght;

    // Clear text data
    public byte[] data;

    // Certificate
    public Certificate certificate;

}
