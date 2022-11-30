package health.medunited.event;

import java.util.Date;

public class NoteEvent {

    private String jwt;
    private String referencedObject;
    private String message;
    private Date date;

    public NoteEvent() {
    }
    public NoteEvent(String jwt, String referencedObject, String message, Date date) {
        this.jwt = jwt;
        this.referencedObject = referencedObject;
        this.message = message;
        this.date = date;
    }
    
    public String getReferencedObject() {
        return this.referencedObject;
    }

    public void setReferencedObject(String referencedObject) {
        this.referencedObject = referencedObject;
    }

    public String getJwt() {
        return this.jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
}
