import java.util.HashMap;

public class Config{
    private String recipient, sender, password, host, port;

    private HashMap<String, String> subject, ticketTypeAcronyms, userTypeAcronyms;

    private String[] validUserTypes;
    private String[] validTicketTypes;

    public Config(){
        recipient = "";
        sender = "";
        password = "";
        host = "";
        port = "";
        subject = new HashMap<>();
        ticketTypeAcronyms = new HashMap<>();
        userTypeAcronyms = new HashMap<>();
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSender(String sender){
        this.sender = sender;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setHost(String host){
        this.host = host;
    }

    public void setPort(String port){
        this.port = port;
    }

    public void setSubject(HashMap<String, String> subject){
        this.subject = subject;
    }

    public void setTicketTypeAcronyms(HashMap<String, String> ticketTypeAcronyms){
        this.ticketTypeAcronyms = ticketTypeAcronyms;
    }

    public void setUserTypeAcronyms(HashMap<String, String> userTypeAcronyms){
        this.userTypeAcronyms = userTypeAcronyms;
    }

    public void setValidTicketTypes(String[] validTicketTypes){
        this.validTicketTypes = validTicketTypes;
    }

    public void setValidUserTypes(String[] validUserTypes){
        this.validUserTypes = validUserTypes;
    }

    public String getHost(){
        return host;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getSender(){
        return sender;
    }

    public String getPassword(){
        return password;
    }

    public String getPort(){
        return port;
    }

    public HashMap<String, String> getSubject(){
        return subject;
    }

    public HashMap<String, String> getTicketTypeAcronyms(){
        return ticketTypeAcronyms;
    }

    public HashMap<String, String> getUserTypeAcronyms(){
        return userTypeAcronyms;
    }

    public String[] getValidUserTypes(){
        return validUserTypes;
    }

    public String[] getValidTicketTypes(){
        return validTicketTypes;
    }
}
