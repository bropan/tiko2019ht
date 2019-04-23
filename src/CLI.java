import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.io.PrintWriter;
import java.io.IOException;
public class CLI {
    private final static String QUIT = "quit";
    private final static String ADD = "add";
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
                ADD + " - Add something"
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
            enterColumns(userInput, tableName, con);
        } else {
            return;
        }

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
        for(int i=1; i <= columnCount; ++i){
            enterColumn(userInput,rs,i,con);
        }

        return true;
    }
    
    private static boolean enterColumn(Scanner userInput, ResultSet rs, int column, Connection con) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        String label = rsmd.getColumnLabel(column);
        String name = rsmd.getColumnName(column);
        System.out.println(label + " " + name);
        return true;
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

    public static String askUser(String question, Scanner s){
        System.out.print(question);
        return s.nextLine();
    }

    public static boolean askYesOrNo(String question, Scanner s){
        System.out.println(question);
        System.out.print("Enter y for YES, n for NO: ");
        while(true){
            switch(s.nextLine().toLowerCase()){
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
