public class Ticket{
    private String ticketNumber;
    private String ticketType;
    private String status;
    private String name;
    private String ID;

    public Ticket(){
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
}
