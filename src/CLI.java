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
    private final static String ADD_WORKSITE = "addworksite";
    private final static String CHARGEABLES = "chargeables";
    private final static String SHOW_TABLE = "showtable";
    private final static String HELP = "help";
    private final static String WRITE_CREDENTIALS = "createlogin";
    private final static String RESET_DATABASE = "resetdatabase";

    private final static String INDENT = "    ";

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
                System.out.println("Unexpected exception happened in the user interface: " + e.getMessage());
                e.printStackTrace();
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
                case ADD_WORKSITE:
                    addWorksite(userInput, con);
                    break;
                case CHARGEABLES:
                    chargeables(userInput, con);
                    break;
                case SHOW_TABLE:
                    showTable(userInput, con);
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
                ADD_WORKSITE + " - Add a new worksite interactively"
                + "\n" +
                CHARGEABLES + " - Add utility/work types or increase their stock amount"
                + "\n" +
                SHOW_TABLE + " - Show data about customers/worksites/bills etc."
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
            System.out.println(INDENT + "[" + (i+1) + "] " + options.get(i));
        }
        System.out.println(INDENT + "[a] ABORT");

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

    public static void showTable(Scanner userInput, Connection con) throws SQLException {

        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "asiakas",
            "tyokohde",
            "tyosuoritus",
            "laskutettava",
            "sopimus",
            "sisaltaa",
            "lasku"
        ));

        int selection = askSelection("Show which table? ", options, userInput);

        Statement showTable = con.createStatement();
        ResultSet rs = showTable.executeQuery("SELECT * FROM " + options.get(selection));
        ResultSetMetaData rsmd = rs.getMetaData();

        int columns = rsmd.getColumnCount();

        for(int i=1; i <= columns; i++) {
            String name = null;
            name = String.format("%12.12s| ", rsmd.getColumnName(i));
            System.out.print(name);
        }

        System.out.println();

        for(int i=1; i<= columns; i++) {
            System.out.print("-------------");
        }

        System.out.println();

        while (rs.next()) {
            String dataRow = "";
            for(int i=1; i <= columns; i++){
                dataRow += String.format("%12.12s| ", rs.getString(i));
            }
            System.out.println(dataRow);
        }
    }

    public static void chargeables(Scanner userInput, Connection con) throws SQLException {

        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "add new work/utility",
            "increase existing stock"
        ));
        int selection = askSelection("What do you want to do?", options, userInput);
        switch(selection){
            case 0:
                addChargeable(userInput, con);
                break;
            case 1:
                searchChargeable(userInput,con);
                break;
        }
    }

    public static void addChargeable(Scanner userInput, Connection con) throws SQLException {
        System.out.println("Adding utility/work that can be charged from the customer");

        String name = askUser("Enter name for the chargeable: ",userInput);
        String unit = askUser("Enter the unit type (kg,m,kpl etc): ",userInput);
        ArrayList<String> types = new ArrayList<>(Arrays.asList(
            "tyo",
            "tarvike"
        ));
        int type = askSelection("Enter the type: ", types, userInput);
        if(type==-1) return;
        String wares = askUser("Enter the amount:",userInput);
        String price = askUser("Enter the price:",userInput);
        String values = String.format(
                "DEFAULT,'%s','%s','%s','%s','%s'"
                ,name,unit,types.get(type),wares,price
                );
        askInsertion(userInput,con,"laskutettava",values);
    }

    //Find chargeable by name
    public static int searchChargeable(Scanner userInput, Connection con) throws SQLException {
        int selection = -1;
        String what = askUser("Enter name of chargeable to search for: ",userInput);
        Statement search = con.createStatement();
        ResultSet rs = search.executeQuery(
                "SELECT * FROM " + "laskutettava" + " WHERE nimi = '" +what+ "'");
        System.out.println("Results:");
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();

        //Print header
        System.out.print(INDENT + "    ");
        for(int i=1; i <= columns; i++) {
            System.out.print(
                String.format( "%12.12s| ", rsmd.getColumnName(i) )
            );
        }
        System.out.println();

        ArrayList<String> values = new ArrayList<>();
        while(rs.next()){
            String row = "";
            for(int i=1; i <= columns; i++) {
                row += String.format("%12.12s| ", rs.getString(i));
            }
            values.add(row);
        }
        if(values.size() > 0)
            selection = askSelection("Select result row -------------------- ",values, userInput);

        return selection;
    }

    public static void increaseChargeableWares(Scanner userInput, Connection con) throws SQLException {

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

    public static void addWorksite(Scanner userInput, Connection con) throws SQLException {
        System.out.println("Adding a new worksite");
        String address = askUser("Enter address: ",userInput);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "kesamokki",
            "asunto",
            "muu"
        ));
        int type = askSelection("Enter customer type: ", options, userInput);
        if(type==-1) return;
        String values = String.format(
                "DEFAULT,'%s','%s'"
                ,address,options.get(type)
                );
        askInsertion(userInput,con,"tyokohde",values);
    }

    public static void askInsertion(Scanner userInput, Connection con, String tableName, String values) throws SQLException {
        if( askYesOrNo("Do you want to add the values (" + values + ") into the table " + tableName + "?", userInput) ){
            Statement insertion = con.createStatement();
            insertion.executeUpdate("INSERT INTO " + tableName + " VALUES (" +values+ ")");
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
