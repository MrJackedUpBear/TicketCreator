package win.servername.TicketCreator;

public class Ticket{
    private final String ticketNumber;
    private String ticketType;
    private String status;
    private String name;
    private String ID;

    public Ticket(){
        ticketNumber = "";
        ticketType = "";
        status = "";
        name = "";
        ID = "";
    }

    public Ticket(String ticketNumber, String ticketType, String status, String name, String ID){
        this.ticketNumber = ticketNumber;
        this.ticketType = ticketType;
        this.status = status;
        this.name = name;
        this.ID = ID;
    }

    public String getTicketNumber(){
        return ticketNumber;
    }

    public String getTicketType(){
        return ticketType;
    }

    public String getStatus(){
        return status;
    }

    public String getName(){
        return name;
    }

    public String getID(){
        return ID;
    }

    public String getSpecifiedValue(String input){
        String val = "";

        if (input.equalsIgnoreCase("Ticket-Type")){
            val = ticketType;
        }else if (input.equalsIgnoreCase("Status")){
            val = status;
        }else if (input.equalsIgnoreCase("Name")){
            val = name;
        }else if (input.equalsIgnoreCase("ID")){
            val = ID;
        }

        return val;
    }

    public void updateSpecifiedValue(String input, String newVal){
        if (input.equalsIgnoreCase("Ticket-Type")){
            ticketType = newVal;
        }else if (input.equalsIgnoreCase("Status")){
            status = newVal;
        }else if (input.equalsIgnoreCase("Name")){
            name = newVal;
        }else if (input.equalsIgnoreCase("ID")){
            ID = newVal;
        }
    }
}
