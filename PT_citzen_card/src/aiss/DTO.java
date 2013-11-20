package aiss;

import java.io.Serializable;

public class DTO
        implements Serializable {
    public MsgType type;
    public Nouce nouce;
    public byte[] signed;
    public Boolean auth;

    public DTO(MsgType type, Nouce nouce, Boolean auth) {
        super();
        this.type = type;
        this.nouce = nouce;
        this.auth = auth;
    }



}
