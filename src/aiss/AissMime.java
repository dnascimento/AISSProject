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

    // EmailTextLenght
    public int emailTextLenght = 0;

    // Zip size
    public int zipLenght = 0;

    // Datasign lenght
    public int dataSignLengh = 0;

    public boolean ciphered = false;


    // Dataciphered: mail|zip|signature
    public byte[] rawdata = null;


    // Timestamp Sign
    public TimestampObject timestamp = null;


    // Certificate
    public X509Certificate certificate;




}
