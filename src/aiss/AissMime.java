package aiss;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import aiss.timestampServer.TimestampObject;

/**
 * DTO for security data
 */
public class AissMime
        implements Serializable {
    private static final long serialVersionUID = -4009171162105117499L;

    public boolean ciphered = false;

    // Dataciphered
    public byte[] data = null;

    // Signature
    public byte[] signature = null;


    // Timestamp Sign
    public TimestampObject timestamp = null;


    // Certificate
    public X509Certificate certificate;




}
