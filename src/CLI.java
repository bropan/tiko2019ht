import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import java.io.PrintWriter;
import java.io.IOException;
public class CLI {
    private final static String QUIT = "quit";
    private final static String ADD = "add";
    private final static String ADD_CUSTOMER = "addcustomer";
    private final static String HELP = "help";
    private final static String WRITE_CREDENTIALS = "createlogin";
    private final static String RESET_DATABASE = "resetdatabase";

    private CLI(){
    }

    public static void run(Scanner userInput, Connection con) throws SQLException {
        System.out.println("Launching Sähkötärsky user interface.");
        System.out.println("Enter " + HELP + " for list of available commands.");
        System.out.println("");

        boolean continueRunning = true;
        while(continueRunning){
            System.out.print("Enter command: ");
            String command = userInput.nextLine();

            try {
                con.setAutoCommit(false);
                System.out.println();
                continueRunning = commandHandler(command,userInput,con);
                con.commit();
            } catch (Exception e) {
                System.out.println("Exception in user interface: " + e.getMessage());
                con.rollback();
                System.out.println("Rolled back, no changes made to database.");
            } finally {
                System.out.println();
                con.setAutoCommit(true);
            }
        }
    }

    private static boolean commandHandler(String command, Scanner userInput, Connection con) throws SQLException {
        boolean continueRunning = true;
            switch(command){
                case QUIT:
                    continueRunning = false;
                    break;
                case ADD:
                    addInteractive(userInput, con);
                    break;
                case ADD_CUSTOMER:
                    addCustomer(userInput, con);
                    break;
                case WRITE_CREDENTIALS:
                    writeCredentialsToFile(userInput, Main.LOGIN_CONFIG_FILE_PATH);
                    break;
                case RESET_DATABASE:
                    resetDatabase(userInput, con);
                    break;
                case HELP:
                    printHelp();
                    break;
                default:
                    System.out.println(
                            "Unknown command: " + command + ". Type " + HELP + " for list of available commands.");
                    break;
            }
        return continueRunning;
    }
    private static void printHelp(){
        System.out.print(
                "\n" +
                "----- Commands -----" + "\n" +

                QUIT + " - Exit program"
                + "\n" +
                ADD + " - Add something (works badly)"
                + "\n" +
                ADD_CUSTOMER + " - Add new customer interactively"
                + "\n" +
                WRITE_CREDENTIALS + " - Create/update a login configuration file" 
                + "\n" +
                RESET_DATABASE  + " - Reset database (warning: removes all entered data!)"
                + "\n" +
                HELP + " - Print this help"
                + "\n" +

                "----- Commands -----" + "\n\n" 
        );
    }

    public static void addInteractive(Scanner userInput, Connection con) throws SQLException {
        System.out.println();
        System.out.println("--- Adding values ---");
        boolean adding = true;
        while(adding){
            ArrayList<String> addOptions = new ArrayList<>(Arrays.asList(
                "asiakas",
                "tyokohde",
                "tyosuoritus",
                "laskutettava"
            ));
            int answer = askSelection("Select what needs to be added ", addOptions, userInput);
            if(answer >= 0){
                String tableName = addOptions.get(answer);
                System.out.println("You answered " + tableName);
                boolean done = enterColumns(userInput, tableName, con);
                if(done){
                    adding = false;
                }
            } else {
                adding = false;
            }
        }

        System.out.println("--------------------");
        System.out.println();
    }

    public static LoginCredentials loginFromUserInput(Scanner userInput){
        LoginCredentials lc = null;
        String username = CLI.askUser("Enter username: ", userInput);
        String password = CLI.askUser("Enter password: ", userInput);
        lc = new LoginCredentials(username,password);
        return lc;
    }

    private static void writeCredentialsToFile(Scanner userInput, String loginConfigFilePath){
        LoginCredentials lc = loginFromUserInput(userInput);
        boolean save = CLI.askYesOrNo(
                "Do you want to enter the used login credentials to '" + loginConfigFilePath + "' (Y/N)",
                userInput
                );
        if(save){
            try (PrintWriter pw = new PrintWriter(loginConfigFilePath)){
                pw.println("username;" + lc.username);
                pw.println("password;" + lc.password);
                System.out.println("Login configuration written to '" + loginConfigFilePath + "'.");
            } catch (IOException e) {
                System.out.println("IOException while saving credentials: " + e.getMessage());
            }
        }
    }

    private static void resetDatabase(Scanner userInput,Connection con) throws SQLException {
        boolean confirm = CLI.askYesOrNo(
                "WARNING: Do you really want to reset the database? "
                +"This will remove all entered data and replace it with default data!",
                userInput
                );
        if(confirm){
            DatabaseStructureHandler.resetDatabase(con, Main.GLOBAL_SCHEMA); 
        }
    }

    //returns false if aborted
    private static boolean enterColumns(Scanner userInput, String tableName, Connection con) throws SQLException {

        Statement getColumns = con.createStatement();
        ResultSet rs = getColumns.executeQuery("SELECT * FROM "+tableName);
        ResultSetMetaData rsmd = rs.getMetaData();

        int columnCount = rsmd.getColumnCount();
        System.out.println(tableName + " has " + columnCount + " columns");

        System.out.println();
        String valuesAsString = "";
        for(int i=1; i <= columnCount; ++i){
            String enteredValue = enterColumn(userInput,rs,i,con);
            valuesAsString += i==1 ? enteredValue : "," + enteredValue;
        }
        System.out.println();
        getColumns.close();

        if(askYesOrNo("Insert " + valuesAsString + " into " + tableName + "?", userInput)){
            Statement insertValues = con.createStatement();
            insertValues.executeUpdate("INSERT INTO " + tableName + " VALUES (" +valuesAsString+")");
            System.out.println("Inserted " + valuesAsString + " into " + tableName);
        } else {
            return false;
        }

        return true;
    }
    
    private static String enterColumn(Scanner userInput, ResultSet rs, int column, Connection con) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        String label = rsmd.getColumnLabel(column);
        String name = rsmd.getColumnName(column);

        if(rsmd.isAutoIncrement(column)){
            return "DEFAULT";
        }

        int type = rsmd.getColumnType(column);
        System.out.println("debug, column name: " + name + " type: " + type);
        boolean entering = true;
        String typeStr = null;
        switch(type){
            case Types.VARCHAR:
                typeStr = "text";
                break;

            case Types.INTEGER:
                typeStr = "number";
                break;
            default:
                System.out.println("UNIDENTIFIED VARIABLE TYPE: " + type);
                typeStr = "(UNIDENTIFIED CONSTANT" + type + ")";
                break;
        }
        String answer = askUser(

                "Enter value for attribute [" + name + "] which is of type " + typeStr + ": "
                , userInput);
        //System.out.println("You typed: '" + answer + "'");

        return "'" + answer + "'";
    }

    public static int askSelection(String question, ArrayList<String> options, Scanner userInput){
        int selection = -1;
        System.out.println(question);
        int optionsLength = options.size();
        if(question==null||options==null||optionsLength==0||userInput==null){
            throw new IllegalArgumentException("Invalid parameters for askSelection!");
        }

        for(int i=0; i < optionsLength; ++i){ //Listing options
            System.out.println("    " + "[" + (i+1) + "] " + options.get(i));
        }
        System.out.println("    " + "[a] ABORT");

        boolean asking = true;
        while(asking){
            System.out.println("Enter choice [1-"+optionsLength+"]: ");
            String answer = userInput.nextLine();
            try {
                int asNumber = Integer.parseInt(answer) - 1;
                if(asNumber >= 0 && asNumber < optionsLength){
                    selection = asNumber;
                    asking = false;
                } else {
                    System.out.println("Select a number from range [1-"+optionsLength+"]!");
                }
            } catch (NumberFormatException nfe){
                if(answer.equals("ABORT") || answer.equals("a")){
                    selection = -1;
                    asking = false;
                } else {

                    int search = options.indexOf(answer);
                    if(search != -1){
                        selection = search;
                        asking = false;
                    } else {
                        System.out.println("Invalid input, "
                                +"type a number or the name of the selection!");
                    }
                }
            }
        }
        return selection;
    }

    public static void addCustomer(Scanner userInput, Connection con) throws SQLException {
        System.out.println("Adding new user");
        String name = askUser("Enter name: ",userInput);
        String address = askUser("Enter address: ",userInput);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "yksityinen",
            "yrittaja"
        ));
        int type = askSelection("Enter customer type: ", options, userInput);
        if(type==-1) return;
        String values = String.format(
                "DEFAULT,'%s','%s','%s'"
                ,name,address,options.get(type)
                );
        askInsertion(userInput,con,"asiakas",values);
    }

    public static void askInsertion(Scanner userInput, Connection con, String tableName, String values) throws SQLException {
        if( askYesOrNo("Do you want to add the values (" + values + ") into the table " + tableName + "?", userInput) ){
            Statement insertion = con.createStatement();
            insertion.executeUpdate("INSERT INTO " + "asiakas" + " VALUES (" +values+ ")");
            insertion.close();
            System.out.println("Values (" + values + ") were added to table "+tableName+".");
        } else {
            System.out.println("Aborted, no changes made.");
        }
    }

    public static String askUser(String question, Scanner s){
        System.out.print(question);
        return s.nextLine();
    }

    public static boolean askYesOrNo(String question, Scanner userInput){
        System.out.println(question);
        System.out.print("Enter y for YES, n for NO: ");
        while(true){
            switch(userInput.nextLine().toLowerCase()){
                case "y":
                case "yes":
                    return true;
                case "n":
                case "no":
                    return false;
                default:
                    System.out.print("Enter y for YES, n for NO: ");
            }
        }
    }

}
