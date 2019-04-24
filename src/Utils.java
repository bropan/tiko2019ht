import java.util.Scanner;

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
                int asNumber = Integer.parseInt(answer) - 1;
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
}
