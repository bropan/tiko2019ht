import java.util.Scanner;

import java.sql.SQLException;
import java.sql.Connection;

import java.io.PrintWriter;
import java.io.IOException;
public class CLI {
    private final static String QUIT = "quit";
    private final static String HELP = "help";
    private final static String WRITE_CREDENTIALS = "createlogin";

    private CLI(){
    }

    public static void run(Scanner userInput, Connection c) throws SQLException {
        System.out.println("Launching Sähkötärsky user interface.");
        System.out.println("Enter " + HELP + " for list of available commands.");
        System.out.println("");

        boolean continueRunning = true;
        while(continueRunning){
            System.out.print("Enter command: ");
            String command = userInput.nextLine();

            try {
                continueRunning = commandHandler(command,userInput);
            } catch (Exception e){
                System.out.println("Exception in user interface: " + e.getMessage());
            }

        }
    }

    private static boolean commandHandler(String command, Scanner userInput){
        boolean continueRunning = true;
            switch(command){
                case QUIT:
                    continueRunning = false;
                    break;
                case WRITE_CREDENTIALS:
                    writeCredentialsToFile(userInput, Main.LOGIN_CONFIG_FILE_PATH);
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
                WRITE_CREDENTIALS + " - Create/update a login configuration file" 
                + "\n" +
                HELP + " - Print this help"
                + "\n" +

                "----- Commands -----" + "\n\n" 
        );
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

    public static String askUser(String question, Scanner s){
        System.out.print(question);
        return s.nextLine();
    }

    public static boolean askYesOrNo(String question, Scanner s){
        System.out.println(question);
        while(true){
            switch(s.nextLine().toLowerCase()){
                case "y":
                case "yes":
                    return true;
                case "n":
                case "no":
                    return false;
                default:
                    System.out.print("Enter y for YES, n for NO:");
            }
        }
    }

}
