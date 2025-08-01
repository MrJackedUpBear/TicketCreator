import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class Main {

    //Tickets file
    private static final String filePath = "TicketsToDo.txt";

    //Placeholder value for better error handling in the future. Currently just allows me to check if
    //there were issues with sending the email. If there are, it won't clear the file.
    private static boolean issues = false;

    //Mail Info
    //This info is better described in the config.ini file.
    private static String recipient = "";
    private static String sender = "";
    private static String password = "";
    private static String host = "";
    private static String port = "";

    private static final Properties properties = System.getProperties();

    //Subject and title info
    private static Map<String, String> subject = new HashMap<>();
    private static Map<String, String> ticketTypeAcronyms = new HashMap<>();
    private static Map<String, String> userTypeAcronyms = new HashMap<>();
    private static String[] validUserTypes;
    private static String[] validTicketTypes;

    public static void main(String[] args){
        assignVariables();

        //Checks if the user did not enter a command and prompts them for a command if they didn't.
        if (args.length == 0){
            Scanner input = new Scanner(System.in);

            String choice = "";

            while (choice.isEmpty()){
                help();
                System.out.println("Choices:");
                System.out.println("Create *To create all tickets on file.*");
                System.out.println("Add *To add a ticket to the file.*");
                System.out.println("Clear *To clear all tickets from the file.*");
                System.out.println("Show *To show all tickets on file.*");
                System.out.println("Delete *To delete a specified ticket from the file.*");
                System.out.println("Edit *To edit a specified ticket on file.*");
                System.out.println("Help *To display the help text above.*");
                System.out.println("What would you like to do?");
                choice = input.nextLine().strip();
            }

            args = new String[1];
            args[0] = choice;
        }

        if (args[0].equalsIgnoreCase("create")){
            createTickets();
        }else if (args[0].equalsIgnoreCase("add")){
            addToFile();
        }else if (args[0].equalsIgnoreCase("clear")){
            clearFile();
        }else if (args[0].equalsIgnoreCase("show")){
            displayFile();
        }else if (args[0].equalsIgnoreCase("delete")){
            deleteTicket();
        }else if (args[0].equalsIgnoreCase("edit")){
            editTicket();
        }else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")){
            help();
            return;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        help();
    }

    //Assigns the initialized variables at the top to values from the config.ini file.
    private static void assignVariables(){
        HashMap<String, HashMap<String, String>> content = getConfig();

        if (content.isEmpty()) {
            System.out.println("Unable to load config. Exiting...");
            System.exit(1);
        }

        //Loads the configs into the variables created above.
        recipient = content.get("Mail Info").get("Recipient");
        sender = content.get("Mail Info").get("Sender");
        password = content.get("Mail Info").get("MailPassword");
        host = content.get("Mail Info").get("MailHost");
        port = content.get("Mail Info").get("MailPort");

        subject = content.get("Ticket Types");
        ticketTypeAcronyms = content.get("Ticket Type Acronyms");
        userTypeAcronyms = content.get("User Type Acronyms");
        validTicketTypes = content.get("Valid Ticket Types").get("csv").split(", ");
        validUserTypes = content.get("Valid User Types").get("csv").split(", ");
    }

    //Gets the config file info.
    private static HashMap<String, HashMap<String, String>> getConfig(){
        //config file.
        String file = "config.ini";

        HashMap<String, HashMap<String, String>> content = new HashMap<>();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            String category = "";
            HashMap<String, String> values = new HashMap<>();

            //Reads through the file and adds everything to the content hashmap.
            while ((line = reader.readLine()) != null){
                if (line.charAt(0) == '['){
                    if (!category.isEmpty()){
                        content.put(category, values);
                    }

                    category = line.substring(1, line.length() - 1);
                }

                if (category.isEmpty() && (line.charAt(0) != '#' && line.charAt(0) != ';')){
                    line = null;
                }else if (line.charAt(0) != '[' && (line.charAt(0) != '#' && line.charAt(0) != ';')){
                    String[] keyValuePair = line.split("=");

                    if (keyValuePair.length != 2){
                        System.out.println("Looks like your config folder is not set up properly. Please ensure " +
                                "that you have your email settings configured properly. Exiting...");
                        System.exit(1);
                    }

                    values.put(keyValuePair[0].strip(), keyValuePair[1].strip());
                }
            }

            content.put(category, values);

            reader.close();
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }

        return content;
    }

    //Creates the tickets by emailing the specified recipient for each ticket on file.
    private static void createTickets(){
        ArrayList<HashMap<String, String>> content = readFile();

        assert content != null;
        if (content.isEmpty()){
            System.out.println("Looks like there are no tickets to complete. Exiting...");
            return;
        }

        //Loads through the TicketsToDo.txt file and checks if the input in the file is valid before sending the email.
        for (HashMap<String, String> c : content){
            String body = "";

            for (int i = 0; i < validTicketTypes.length; i++){
                if (validTicketTypes[i].equals(c.get("Ticket-Type"))){
                    body = subject.get(c.get("Ticket-Type"));
                    i = validTicketTypes.length;
                }else if (i == validTicketTypes.length - 1){
                    String ticketType = ticketTypeAcronyms.get(c.get("Ticket-Type").toUpperCase());

                    if (ticketType != null){
                        body = subject.get(ticketType);
                    }else{
                        body = null;
                    }
                }
            }

            if (body == null){
                System.out.println("Unknown Subject: " + c.get("Ticket-Type"));
                issues = true;
            }else if (c.get("Ticket-Type") == null || c.get("Status") == null || c.get("Name") == null || c.get("Id") == null){
                System.out.println("Did not provide all parameters for type: " + body);
                issues = true;
            }else{
                StringBuilder title = new StringBuilder();
                for (int i = 0; i < validTicketTypes.length; i++){
                    if (validTicketTypes[i].equalsIgnoreCase(c.get("Ticket-Type"))){
                        title.append(validTicketTypes[i]);
                        i = validTicketTypes.length;
                    }else if (i == validTicketTypes.length - 1){
                        title.append(ticketTypeAcronyms.get(c.get("Ticket-Type").toUpperCase()));
                    }
                }

                title.append(" - ");
                for (int i = 0; i < validUserTypes.length; i++){
                    if (validUserTypes[i].equalsIgnoreCase(c.get("Status"))){
                        assert title != null;
                        title.append(validUserTypes[i]);
                        i = validTicketTypes.length;
                    }else if (i == validUserTypes.length - 1){
                        String test = userTypeAcronyms.get(c.get("Status").toUpperCase());

                        if (test == null){
                            System.out.println("Unknown User Type: " + c.get("Status"));
                            title = null;
                        }else{
                            assert title != null;
                            title.append(test);
                        }
                    }
                }

                if (title != null ){
                    title.append(" - ").append(c.get("Name")).append(" - ").append(c.get("Id"));
                    sendEmail(title.toString(), body);
                }else{
                    issues = true;
                }
            }
        }

        if (!issues){
            System.out.println("Clearing file...");
            clearFile();
        }else{
            System.out.println("Seems there were issues with the document. Did not clear file.");
        }
    }

    //Updates info on TicketsToDo.txt file.
    private static void editTicket(){
        String ticketNumInput = "";
        int numLoc = 0;

        ArrayList<HashMap<String, String>> content = readFile();

        assert content != null;
        if (content.isEmpty()){
            displayFile();
            System.out.println("Exiting...");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (ticketNumInput.isEmpty()){
            boolean foundNum = false;
            displayFile();
            System.out.println("Enter the ticket number of the ticket you would like to update: ");
            ticketNumInput = scanner.nextLine();

            if (ticketNumInput.equalsIgnoreCase("exit")){
                scanner.close();
                System.out.println("Exiting...");
                return;
            }

            int j = 0;
            for (HashMap<String, String> c : content){
                if (c.get("Ticket-Number").equals(ticketNumInput)){
                    numLoc = j;
                    foundNum = true;
                    break;
                }
                j++;
            }

            if (foundNum){
                System.out.println("Are you sure you want to edit Ticket Number " + ticketNumInput + "? (Defaults to no)");
                String yesOrNo = scanner.nextLine();

                if (Objects.equals(yesOrNo, "")){
                    ticketNumInput = "";
                }else if (yesOrNo.charAt(0) != 'Y' && yesOrNo.charAt(0) != 'y'){
                    ticketNumInput = "";
                }
            }else{
                ticketNumInput = "";
            }
        }

        String valueToUpdate = "";
        String newValue = "";

        Map<String, String> valueOptions = Map.of(
                "1", "Ticket-Type",
                "2", "Status",
                "3", "Name",
                "4", "Id"
        );

        while (valueToUpdate.isEmpty() && newValue.isEmpty()){
            System.out.println("You can update the following:");
            System.out.println("1: Ticket-Type");
            System.out.println("2: Status");
            System.out.println("3: Name");
            System.out.println("4: Id");
            System.out.println("Which would you like to update (i.e. \"3\" to update name)?");
            valueToUpdate = scanner.nextLine();

            if (valueToUpdate.equalsIgnoreCase("exit")){
                scanner.close();
                System.out.println("Exiting...");
                return;
            }

            for (int i = 1; i < 5; i++){
                if (valueToUpdate.equals(String.valueOf(i))){
                    valueToUpdate = valueOptions.get(valueToUpdate);
                    i = 5;
                }else if (i == 4){
                    valueToUpdate = "";
                }
            }

            if (!Objects.equals(valueToUpdate, "")){
                System.out.println("Ticket values:");
                System.out.println(content.get(numLoc).get("Ticket-Number") + ". " + content.get(numLoc).get("Ticket-Type") + ", " + content.get(numLoc).get("Status") + ", " +
                        content.get(numLoc).get("Name") + ", " + content.get(numLoc).get("Id"));
                System.out.println("Currently editing: ");
                System.out.println("Ticket-Number " + content.get(numLoc).get("Ticket-Number") + " " + valueToUpdate + ": " + content.get(numLoc).get(valueToUpdate));

                System.out.println("Enter the updated value:");
                newValue = scanner.nextLine();

                if (newValue.equalsIgnoreCase("exit")){
                    scanner.close();
                    System.out.println("Exiting...");
                    return;
                }

                if (valueToUpdate.equals("Ticket-Type")){
                    if (!containsTicketType(newValue)){
                        newValue = "";
                    }
                }else if (valueToUpdate.equals("Status")){
                    if (!containsUserType(newValue)){
                        newValue = "";
                    }
                }
            }
        }

        scanner.close();

        int currentLine = 0;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));

            String line;
            int i = 0;
            while ((line = reader.readLine()) != null){
                if (i != 0){
                    writer.newLine();
                    currentLine = line.charAt(0) - '0';
                }

                if (Integer.parseInt(ticketNumInput) != currentLine){
                    writer.write(line);
                    currentLine++;
                }else{
                    String valueToWrite = "";

                    String[] temp = line.split(content.get(numLoc).get(valueToUpdate) + ",");

                    valueToWrite += temp[0];
                    valueToWrite += newValue + ",";
                    valueToWrite += temp[1];

                    writer.write(valueToWrite);
                }
                i++;
            }

            reader.close();
            writer.close();

            Path file = Paths.get(filePath);
            Path oldPath = Paths.get("temp.txt");

            Files.move(oldPath, file, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully updated ticket.");
            displayFile();
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Adds info to TicketsToDo.txt file.
    private static void addToFile(){
        Scanner scanner = new Scanner(System.in);

        String ticketType = "";
        while (ticketType.isEmpty()){
            System.out.println("Enter Ticket-Type (i.e. PR or Password Reset): ");
            ticketType = scanner.nextLine().strip();

            if (ticketType.equalsIgnoreCase("exit")){
                scanner.close();
                return;
            }

            if (!containsTicketType(ticketType)){
                ticketType = "";
            }
        }

        String userType = "";
        while (userType.isEmpty()){
            System.out.println("Enter User Type (i.e. ES or Enrolled Student): ");
            userType = scanner.nextLine().strip();

            if (userType.equalsIgnoreCase("exit")){
                scanner.close();
                return;
            }

            if (!containsUserType(userType)){
                userType = "";
            }
        }

        String name = "";
        while (name.isEmpty()){
            System.out.println("Enter name: ");
            name = scanner.nextLine().strip();

            if (name.equalsIgnoreCase("exit")){
                scanner.close();
                return;
            }
        }

        String id = "";
        while (id.isEmpty()){
            System.out.println("Enter ID number: ");
            id = scanner.nextLine().strip();

            if (id.equalsIgnoreCase("exit")){
                scanner.close();
                return;
            }
        }

        scanner.close();

        String tempFile = "tempFile.txt";

        Path tempPath = Paths.get(tempFile);
        Path file = Paths.get(filePath);

        int ticketNum = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String line;
            while ((line = reader.readLine()) != null){
                if (!line.equals("Ticket-Number, Ticket-Type, Status, Name, Id")){
                    ticketNum = line.charAt(0) - '0';
                }
                writer.write(line);
                writer.newLine();
            }

            ticketNum++;

            reader.close();

            writer.write(ticketNum + ", " + ticketType + ", " + userType + ", " + name + ", " + id);

            writer.close();

            Files.move(tempPath, file, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File successfully updated.");
            System.out.println();
            displayFile();
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void clearFile(){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Ticket-Number, Ticket-Type, Status, Name, Id");

            writer.close();
            System.out.println("Successfully cleared file.");
            displayFile();
        }catch(Exception e){
            System.out.println("Error clearing file: " + e.getMessage());
        }
    }

    private static void deleteTicket(){
        String ticketNumInput = "";

        ArrayList<HashMap<String, String>> content = readFile();

        assert content != null;
        if (content.isEmpty()){
            displayFile();
            System.out.println("Exiting...");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (ticketNumInput.isEmpty()){
            boolean foundNum = false;
            displayFile();
            System.out.println("Enter the ticket number of the ticket you would like to delete: ");
            ticketNumInput = scanner.nextLine();

            if (ticketNumInput.equalsIgnoreCase("exit")){
                scanner.close();
                System.out.println("Exiting...");
                return;
            }

            for (HashMap<String, String> c : content){
                if (c.get("Ticket-Number").equals(ticketNumInput)){
                    foundNum = true;
                    break;
                }
            }

            if (foundNum){
                System.out.println("Are you sure you want to delete Ticket Number " + ticketNumInput + "? (Defaults to no)");
                String yesOrNo = scanner.nextLine();

                if (Objects.equals(yesOrNo, "")){
                    ticketNumInput = "";
                }else if (yesOrNo.charAt(0) != 'Y' && yesOrNo.charAt(0) != 'y'){
                    ticketNumInput = "";
                }
            }else{
                ticketNumInput = "";
            }
        }

        scanner.close();

        int currentLine = 1;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));

            String line;
            int i = 0;
            while ((line = reader.readLine()) != null){
                currentLine = line.charAt(0) - '0';
                if (Integer.parseInt(ticketNumInput) != currentLine){
                    if (i != 0){
                        writer.newLine();
                    }
                    writer.write(line);
                }
                currentLine++;
                i++;
            }

            reader.close();
            writer.close();

            Path file = Paths.get(filePath);
            Path oldPath = Paths.get("temp.txt");

            Files.move(oldPath, file, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully deleted ticket.");
            displayFile();
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Shows the user the info from the TicketsToDo.txt file.
    private static void displayFile(){
        ArrayList<HashMap<String, String>> content = readFile();

        assert content != null;
        if (content.isEmpty()){
            System.out.println("The file is empty.");
            return;
        }

        System.out.println("Tickets Needing to be Created:");
        System.out.println("Ticket-Number, Ticket-Type, Status, Name, Id");
        for (HashMap<String, String> c : content){
            System.out.println(c.get("Ticket-Number") + ". " + c.get("Ticket-Type") + ", " + c.get("Status") + ", " + c.get("Name") + ", " + c.get("Id"));
        }
    }

    private static void help(){
        for (int i = 0; i < 6; i++){
            System.out.println();
        }
        System.out.println("Help:");
        try{
            BufferedReader reader = new BufferedReader(new FileReader("HowTo.txt"));

            String line;

            while ((line = reader.readLine()) != null){
                System.out.println(line);
            }

            reader.close();
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Reads from the TicketsToDo.txt file.
    private static ArrayList<HashMap<String, String>> readFile(){
        ArrayList<HashMap<String, String>> content = new ArrayList<>();

        ArrayList<String> headers = new ArrayList<>();
        int len = 0;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null){
                ArrayList<String> lineItems = new ArrayList<>(Arrays.asList(line.split(", ")));

                if (lineNum == 0){
                    headers = lineItems;
                }else{
                    HashMap<String, String> ticketInfo = new HashMap<>();
                    len = lineItems.size();
                    for (int i = 0; i < len; i++){
                        ticketInfo.put(headers.get(i), lineItems.get(i));
                    }
                    content.add(ticketInfo);
                }

                lineNum++;
            }

            reader.close();

            //System.out.println("Successfully read from file.");
            //Path oldFile = Paths.get(filePath);
            //Path newFile = Paths.get("TicketsToDo.txt.old");

            //try{
            //    Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
            //    System.out.println("Successfully renamed file.");
            //} catch(Exception e){
            //    System.out.println("Error: " + e.getMessage());
            //}

            return content;
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private static void sendEmail(String title, String body){
        try{
            //sets up the mail properties
            properties.put("mail.smtp.auth", true);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.ssl.trust", host);

            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(sender, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(title);
            message.setText(body);
            System.out.println("Sending email...");
            Transport.send(message);
            System.out.println("Mail successfully sent");
        }catch(Exception e){
            System.out.println("Error for " + title + ": " + e.getMessage());
            issues = true;
        }
    }

    //Checks if the input is a ticket type.
    private static Boolean containsTicketType(String input){
        for (int i = 0; i < validTicketTypes.length; i++){
            if (validTicketTypes[i].equals(input)){
                return true;
            }else if (i == validTicketTypes.length - 1){
                String ticketType = ticketTypeAcronyms.get(input.toUpperCase());

                if (ticketType != null){
                    return true;
                }else{
                    System.out.println("Unknown Ticket Type: " + input);
                    return false;
                }
            }
        }

        return false;
    }

    //Checks if the input is a user type.
    private static Boolean containsUserType(String input){
        for (int i = 0; i < validUserTypes.length; i++){
            if (validUserTypes[i].equals(input)){
                return true;
            }else if (i == validUserTypes.length - 1){
                String test = userTypeAcronyms.get(input.toUpperCase());

                if (test == null){
                    System.out.println("Unknown User Type: " + input);
                    return false;
                }else{
                    return true;
                }
            }
        }

        return false;
    }
}
