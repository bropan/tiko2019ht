import java.util.Scanner;
import java.io.PrintWriter;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
public class Utils {
    //cannot be instantiated

    private final static String INDENT = "    ";

    private Utils(){
    }
    public static boolean isNullOrEmpty(String s){
        if(s == null || s.equals("")){
            return true;
        } else {
            return false;
        }
    }

    public static String askUser(String question, Scanner userInput){
        System.out.print(question);
        return userInput.nextLine();
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

    public static int askSelection(String question, HashMap<Integer, String> options, Scanner userInput){
        int selection = -1;
        System.out.println(question);
        int optionsLength = options.size();
        if(question==null||options==null||optionsLength==0||userInput==null){
            throw new IllegalArgumentException("Invalid parameters for askSelection!");
        }

        for(Map.Entry<Integer, String> e : options.entrySet()){
            System.out.println(INDENT + "[" + e.getKey() + "] " + e.getValue());
        }
        System.out.println(INDENT + "[a] ABORT");

        boolean asking = true;
        while(asking){
            System.out.println("Enter choice: ");
            String answer = userInput.nextLine();
            try {
                int asNumber = Integer.parseInt(answer);
                if(options.get(asNumber) != null){
                    selection = asNumber;
                    asking = false;
                } else {
                    System.out.println("Select a number mentioned above.");
                }
            } catch (NumberFormatException nfe){
                if(answer.equals("ABORT") || answer.equals("a")){
                    selection = -1;
                    asking = false;
                } else {
                    System.out.println("Type a number or abort");
                }
            }
        }
        return selection;
    }

    public static void askInsertion(Scanner userInput, String tableName, String values) throws SQLException {
        if( Utils.askYesOrNo("Do you want to add the values (" + values + ") into the table " + tableName + "?", userInput) ){
            Statement insertion = Global.dbConnection.createStatement();
            insertion.executeUpdate("INSERT INTO " + tableName + " VALUES (" +values+ ")");
            insertion.close();
            System.out.println("Values (" + values + ") were added to table "+tableName+".");
        } else {
            System.out.println("Aborted, no changes made.");
        }
    }

    public static boolean report(String content, String fileName){
        if(content == null) throw new IllegalArgumentException("Illegal report content: NULL");
        if(fileName == null) throw new IllegalArgumentException("Illegal report filename: NULL");
        if(fileName == "") throw new IllegalArgumentException("Illegal report filename: Empty");
        if(writeReport(content, fileName)) {
            System.out.println();
            System.out.println(content);
            System.out.println("Report printed succesfully.");
            return true;
        } else {
            System.out.println("Report print failed.");
            return false;
        }
    }

    private static boolean writeReport(String content, String fileName){
        if(content==null) throw new IllegalArgumentException("Attempting to write a report with NULL content!");

        System.out.println("Writing to file " + fileName + "...");
        try {
            PrintWriter pw = new PrintWriter(fileName);
            pw.println(content);
            pw.close();
            System.out.println("File written. Report created.");
        } catch (Exception e){
            System.out.println("Error while writing " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
