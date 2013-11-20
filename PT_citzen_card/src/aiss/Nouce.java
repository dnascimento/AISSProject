package aiss;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;


public class Nouce
        implements Serializable {
    private static final long serialVersionUID = 1L;
    public byte[] nouce;
    public Date timestamp;
    public byte[] sign;

    public Nouce(byte[] nouce) {
        super();
        this.nouce = nouce;
        timestamp = new Date();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Nouce)) {
            return false;
        }
        Nouce newNouce = (Nouce) obj;
        if (!Arrays.equals(nouce, newNouce.nouce)) {
            return false;
        }
        if (!newNouce.timestamp.equals(timestamp)) {
            return false;
        }
        return true;
    }

}
