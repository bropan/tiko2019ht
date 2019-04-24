import java.util.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.math.BigDecimal;

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
    private final static String HELP = "help";
    private final static String REPORT_ONE = "r1";
    private final static String REPORT_TWO = "r2";
    private final static String ADD = "add";
    private final static String ADD_CUSTOMER = "addcustomer";
    private final static String ADD_WORKSITE = "addworksite";
    private final static String CHARGEABLES = "chargeables";
    private final static String CHECKOUT = "checkout";
    private final static String SHOW_TABLE = "showtable";
    private final static String WRITE_CREDENTIALS = "createlogin";
    private final static String RESET_DATABASE = "resetdatabase";

    private final static String INDENT = "    ";

    private CLI(){
    }

    public static void run(Scanner userInput) throws SQLException {
        System.out.println("Launching Sähkötärsky user interface.");
        System.out.println("Enter " + HELP + " for list of available commands.");
        System.out.println("");



        boolean continueRunning = true;
        while(continueRunning){
            if(Global.dbConnection == null){
                throw new IllegalArgumentException("Connection NULL in CLI!");
            }
            System.out.print("Enter command: ");
            String command = userInput.nextLine();

            try {
                Global.dbConnection.setAutoCommit(false);
                System.out.println();
                continueRunning = commandHandler(command,userInput);
                Global.dbConnection.commit();
            } catch (SQLException e) {
                System.out.println("Unexpected SQLException happened in the user interface: " + e.getMessage());
                Global.dbConnection.rollback();
                System.out.println("Rolled back, no changes made to database.");
            } catch (Exception e) {
                System.out.println("Unexpected exception happened in the user interface: " + e.getMessage());
                e.printStackTrace();
                Global.dbConnection.rollback();
                System.out.println("Rolled back, no changes made to database.");
            } finally {
                System.out.println();
                Global.dbConnection.setAutoCommit(true);
            }
        }
    }

    private static boolean commandHandler(String command, Scanner userInput) throws SQLException {
        boolean continueRunning = true;
            switch(command){
                case QUIT:
                    continueRunning = false;
                    break;
                case REPORT_ONE:
                    reportOne();
                    break;
                case REPORT_TWO:
                    System.out.println("not implemented");
                    break;
                case ADD:
                    addInteractive(userInput);
                    break;
                case ADD_CUSTOMER:
                    addCustomer(userInput);
                    break;
                case ADD_WORKSITE:
                    addWorksite(userInput);
                    break;
                case CHARGEABLES:
                    chargeables(userInput);
                    break;
                case CHECKOUT:
                    checkout(userInput);
                    break;
                case SHOW_TABLE:
                    showTableInteractive(userInput);
                    break;
                case WRITE_CREDENTIALS:
                    writeCredentialsToFile(userInput, Main.LOGIN_CONFIG_FILE_PATH);
                    break;
                case RESET_DATABASE:
                    resetDatabase(userInput);
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
                REPORT_ONE + " - Print and write report one."
                + "\n" +
                REPORT_TWO + " - Print and write report two."
                + "\n" +
                ADD + " - Add something (works badly)"
                + "\n" +
                ADD_CUSTOMER + " - Add new customer interactively"
                + "\n" +
                ADD_WORKSITE + " - Add a new worksite interactively"
                + "\n" +
                CHARGEABLES + " - Add utility/work types or change their stock amount"
                + "\n" +
                CHECKOUT + " - Checkout work done and utilities used"
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

    public static void reportOne() throws SQLException {
        final String fileName = "raportti_1.txt";
        System.out.println("Creating report 1");
        System.out.println("");
        String[] r1items = {
            "suunnittelu",
            "asennus",
            "sahkojohto",
            "pistorasia",
        };

        int[] r1counts = {
            3,
            12,
            3,
            1
        };
        String estimate = priceEstimate(r1items,r1counts);
        if(estimate != null){
            System.out.println(estimate);
            System.out.println("Writing to file " + fileName + "...");
            try {
                PrintWriter pw = new PrintWriter(fileName);
                pw.println(estimate);
                pw.close();
                System.out.println("File written. Report created.");
            } catch (Exception e){
                System.out.println("Error when writing " + fileName + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Report creation failed.");
        }
    }

    public static void addInteractive(Scanner userInput) throws SQLException {
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
            int answer = Utils.askSelection("Select what needs to be added ", addOptions, userInput);
            if(answer >= 0){
                String tableName = addOptions.get(answer);
                System.out.println("You answered " + tableName);
                boolean done = enterColumns(userInput, tableName);
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
        String username = Utils.askUser("Enter username: ", userInput);
        String password = Utils.askUser("Enter password: ", userInput);
        lc = new LoginCredentials(username,password);
        return lc;
    }

    private static void writeCredentialsToFile(Scanner userInput, String loginConfigFilePath){
        LoginCredentials lc = loginFromUserInput(userInput);
        boolean save = Utils.askYesOrNo(
                "Do you want to enter the used login credentials to '" + loginConfigFilePath + "' (Y/N)",
                userInput
                );
        if(save){
            try (PrintWriter pw = new PrintWriter(loginConfigFilePath)){
                pw.println("username;" + lc.username);
                pw.println("password;" + lc.password);
                pw.close();
                System.out.println("Login configuration written to '" + loginConfigFilePath + "'.");
            } catch (IOException e) {
                System.out.println("IOException while saving credentials: " + e.getMessage());
            }
        }
    }

    private static void resetDatabase(Scanner userInput) throws SQLException {
        boolean confirm = Utils.askYesOrNo(
                "WARNING: Do you really want to reset the database? "
                +"This will remove all entered data and replace it with default data!",
                userInput
                );
        if(confirm){
            DatabaseStructureHandler.resetDatabase(Main.GLOBAL_SCHEMA); 
        }
    }

    //returns false if aborted
    private static boolean enterColumns(Scanner userInput, String tableName ) throws SQLException {

        Statement getColumns = Global.dbConnection.createStatement();
        ResultSet rs = getColumns.executeQuery("SELECT * FROM "+tableName);
        ResultSetMetaData rsmd = rs.getMetaData();

        int columnCount = rsmd.getColumnCount();
        System.out.println(tableName + " has " + columnCount + " columns");

        System.out.println();
        String valuesAsString = "";
        for(int i=1; i <= columnCount; ++i){
            String enteredValue = enterColumn(userInput,rs,i);
            valuesAsString += i==1 ? enteredValue : "," + enteredValue;
        }
        System.out.println();
        getColumns.close();

        if(Utils.askYesOrNo("Insert " + valuesAsString + " into " + tableName + "?", userInput)){
            Statement insertValues = Global.dbConnection.createStatement();
            insertValues.executeUpdate("INSERT INTO " + tableName + " VALUES (" +valuesAsString+")");
            System.out.println("Inserted " + valuesAsString + " into " + tableName);
        } else {
            return false;
        }

        return true;
    }
    
    private static String enterColumn(Scanner userInput, ResultSet rs, int column ) throws SQLException {
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
        String answer = Utils.askUser(

                "Enter value for attribute [" + name + "] which is of type " + typeStr + ": "
                , userInput);
        //System.out.println("You typed: '" + answer + "'");

        return "'" + answer + "'";
    }



    public static void showTableInteractive(Scanner userInput) throws SQLException {

        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "asiakas",
            "tyokohde",
            "tyosuoritus",
            "laskutettava",
            "sopimus",
            "sisaltaa",
            "lasku"
        ));

        int selection = Utils.askSelection("Show which table? ", options, userInput);
        if(selection>=0){
            showTable(options.get(selection));
        } else {
            System.out.println("Aborted showtable");
        }
    }

    public static void showTable(String tableName) throws SQLException {
        showCustomQuery("SELECT * FROM " + tableName);

    }

    public static void chargeables(Scanner userInput ) throws SQLException {

        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "add new work/utility",
            "change stock"
        ));
        int selection = Utils.askSelection("What do you want to do?", options, userInput);
        switch(selection){
            case 0:
                addChargeable(userInput);
                break;
            case 1:
                int id = searchChargeable(userInput);
                if(id>=0){
                    changeChargeableWares(userInput,id);
                }
                break;
        }
    }

    public static void addChargeable(Scanner userInput ) throws SQLException {
        System.out.println("Adding utility/work that can be charged from the customer");

        String name = Utils.askUser("Enter name for the chargeable: ",userInput);
        String unit = Utils.askUser("Enter the unit type (kg,m,kpl etc): ",userInput);
        ArrayList<String> types = new ArrayList<>(Arrays.asList(
            "tyo",
            "tarvike"
        ));
        int type = Utils.askSelection("Enter the type: ", types, userInput);
        if(type==-1) return;
        String wares = Utils.askUser("Enter the amount:",userInput);
        String price = Utils.askUser("Enter the price:",userInput);
        String values = String.format(
                "DEFAULT,'%s','%s','%s','%s','%s'"
                ,name,unit,types.get(type),wares,price
                );
        Utils.askInsertion(userInput,"laskutettava",values);
    }

    //Find chargeable by name
    public static int searchChargeable(Scanner userInput ) throws SQLException {
        int selection = -1;
        String what = Utils.askUser("Enter name of chargeable to search for: ",userInput);
        Statement search = Global.dbConnection.createStatement();
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

        HashMap<Integer,String> valueMap = new HashMap<>();
        while(rs.next()){
            int id = rs.getInt("laskutettava_id");
            String row = "";
            for(int i=1; i <= columns; i++) {
                row += String.format("%12.12s| ", rs.getString(i));
            }
            valueMap.put(id,row);
        }
        if(valueMap.size() > 0)
            selection = Utils.askSelection("Select result row -------------------- ",valueMap, userInput);

        return selection;
    }

    public static void checkout(Scanner userInput) throws SQLException {
        final String SET_WORKSITE = "set worksite worked on today";
        final String SET_CONTRACT = "set contract this work was related to";
        final String ADD_UTILITY_SEARCH = "add utility or work with name search";
        final String ADD_UTILITY_ID = "add utility or work by id";
        final String CHECKOUT_DONE = "done, save reported work permanently";
        System.out.println("Checking out work/utilities used today.");
        System.out.println("Make sure that the worksite has been created, "+
                "and that the utilities/work have been entered into the system.");

        Statement createWork = Global.dbConnection.createStatement();
        createWork.executeUpdate("INSERT INTO tyosuoritus VALUES (" +
                    String.format("DEFAULT,NULL,NULL") +
                    ")"
        );
        createWork.close();


        Statement getWorkId = Global.dbConnection.createStatement();
        ResultSet getWorkIdResult = getWorkId.executeQuery(
                "SELECT MAX(tyosuoritus_id) as id FROM tyosuoritus"
        );
        getWorkIdResult.next();
        int workId = getWorkIdResult.getInt("id");

        int workSiteId = -1;
        int contractId = -1;
        String workSiteAddress = "";
        boolean checkingOut = true;
        while(checkingOut){
            int utilityId = -1;
            System.out.println();
            
            System.out.println("############## Utilities to be added ###################");
            System.out.println("#                                                      #");
            showCustomQuery(
                    "SELECT s.laskutettava_id, l.nimi, s.lkm, s.alennus " +
                    "FROM " +
                    "laskutettava as l INNER JOIN sisaltaa as s " +
                    "ON l.laskutettava_id = s.laskutettava_id " +
                    "WHERE tyosuoritus_id = " + workId
            );
            System.out.println("#                                                      #");
            System.out.println("########################################################");

            System.out.println("Worksite selected: { "+workSiteAddress+" }");
            System.out.println("Contract selected: ["+contractId+"]");
            ArrayList<String> actions = new ArrayList<>(
                Arrays.asList(
                    SET_WORKSITE,
                    SET_CONTRACT,
                    ADD_UTILITY_SEARCH,
                    ADD_UTILITY_ID,
                    CHECKOUT_DONE
                )
            );
            int action = Utils.askSelection("Select action: ", actions, userInput);
            if(action < 0){
                throw new IllegalArgumentException("Checkout aborted by user.");
            }
            try {
                String answer = "";
                int selected = -1;
                switch(actions.get(action)){
                    case SET_WORKSITE:
                        showTable("tyokohde");
                        answer = Utils.askUser("Type worksite id: ", userInput);
                        selected = Integer.parseInt(answer);
                        Statement getWorksite = Global.dbConnection.createStatement();
                        ResultSet wsrs = getWorksite.executeQuery("SELECT * FROM tyokohde "+
                                "WHERE kohde_id = "+selected);
                        if(wsrs.next()){
                            workSiteId = wsrs.getInt("kohde_id");
                            workSiteAddress = wsrs.getString("osoite");
                        } else {
                            System.out.println("Not found!");
                        }
                        getWorksite.close();
                        break;
                    case SET_CONTRACT:
                        showCustomQuery(
                                "SELECT sopimus.*, asiakas.nimi as asiakasnimi " +
                                "FROM asiakas INNER JOIN sopimus " +
                                "ON asiakas.asiakas_id = sopimus.asiakas_id "
                                );
                        answer = Utils.askUser("Type contract id: ", userInput);
                        selected = Integer.parseInt(answer);
                        Statement getContract = Global.dbConnection.createStatement();
                        ResultSet cors = getContract.executeQuery("SELECT * FROM sopimus "+
                                "WHERE sopimus_id = "+selected);
                        if(cors.next()){
                            contractId = cors.getInt("sopimus_id");
                        } else {
                            System.out.println("Not found!");
                        }
                        getContract.close();
                        break;
                    case ADD_UTILITY_SEARCH: //utilityId handled later in this function
                        utilityId = searchChargeable(userInput);
                        break;
                    case ADD_UTILITY_ID: //utilityId handled later in this function
                        showTable("laskutettava");
                        answer = Utils.askUser("Type chargeable utility id: ", userInput);
                        selected = Integer.parseInt(answer);
                        Statement getChargeable = Global.dbConnection.createStatement();
                        ResultSet chrs = getChargeable.executeQuery("SELECT * FROM laskutettava "+
                                "WHERE laskutettava_id = "+selected);
                        if(chrs.next()){
                            utilityId = chrs.getInt("laskutettava_id");
                        } else {
                            System.out.println("Not found!");
                        }
                        getChargeable.close();
                        break;
                    case CHECKOUT_DONE:
                        System.out.println("Making sure worksite and contract id is right...");
                        if(workSiteId < 0) throw new IllegalArgumentException("Invalid worksiteId!");
                        if(contractId < 0) throw new IllegalArgumentException("Invalid contractId!");
                        System.out.println("You are going to add the above items/work permanently to a bill.");
                        if(Utils.askYesOrNo("Confirm?", userInput)){
                            Statement updateWork = Global.dbConnection.createStatement();
                            updateWork.executeUpdate(
                                "UPDATE tyosuoritus SET sopimus_id = "+contractId+" WHERE tyosuoritus_id = "+workId
                            );
                            updateWork.executeUpdate(
                                "UPDATE tyosuoritus SET kohde_id = "+workSiteId+" WHERE tyosuoritus_id = "+workId
                            );
                            System.out.println("Work has been checked out succesfully.");
                            checkingOut = false;
                        }
                        break;
                    default:
                        System.out.println("Unidentified action!");
                        break;
                }
                if(utilityId > 0){
                    int amount = Integer.parseInt(Utils.askUser(
                                "Enter amount of work/utility to be added: ",userInput));
                    Statement getAmount = Global.dbConnection.createStatement();
                    ResultSet utrs = getAmount.executeQuery(
                            "SELECT varastotilanne FROM laskutettava "+
                            "WHERE laskutettava_id = " +utilityId);
                    utrs.next();
                    int stock = utrs.getInt("varastotilanne");
                    if(!utrs.wasNull() && stock - amount < 0){
                        throw new IllegalArgumentException(
                                "Not enough of utility in stock! " +stock+"-"+amount+"="+(stock - amount));
                    }
                    Double discount = Double.parseDouble(Utils.askUser(
                                "Enter discount for this utility from range [0-1]: ",userInput));
                    if(discount < 0 || discount > 1){
                        throw new IllegalArgumentException(
                                "Discount: Enter a value from range [0-1]");
                    }
                    addUtilityToWork(workId, utilityId, amount, discount);
                    getAmount.close();
                }
            } catch (NumberFormatException nfe){
                System.out.println("Enter a number!");
            } catch (IllegalArgumentException iae){
                System.out.println("---- ERROR: " + iae.getMessage() +"----");
            }
        }
    }

    public static String priceEstimate(String[] chargeables, int[] counts) throws SQLException {
        String estimate = "";
        estimate += String.format("%12.12s#%12.12s#%12.12s\n", 
                "##################","##################","##################");
        estimate += "HINTA-ARVIO KOHTEELLE X\n";

        estimate += String.format("%12.12s#%12.12s#%12.12s\n", 
                "##################","##################","##################");
        estimate += "\n";
        estimate += String.format("%12.12s|%12.12s|%12.12s\n", "Tuotenimi", "Hinta", "Määrä");
        estimate += String.format("%12.12s+%12.12s+%12.12s\n", 
                "----------------", "-----------------", "-----------------");
        BigDecimal totalPrice = new BigDecimal(0);
        for(int i=0; i < chargeables.length; ++i){
            String s = chargeables[i];
            int count = counts[i];
            Statement st = Global.dbConnection.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT nimi, yksikko, MIN(sisaanostohinta) as hinta " +
                    "FROM laskutettava "+
                    "WHERE nimi = '" + s + "' "+
                    "AND " +
                    "( " +
                    "(varastotilanne IS NOT null AND varastotilanne - "+count+" > 0) " +
                    "OR " +
                    "tyyppi = 'tyo'" +
                    ") " +
                    "GROUP BY nimi, yksikko"
            );
            if(rs.next()){
                BigDecimal price = rs.getBigDecimal("hinta");
                price = price.multiply(new BigDecimal(Global.priceMultiplier));
                String name = rs.getString("nimi");
                String unit = rs.getString("yksikko");
                String countStr = count + unit;
                estimate += String.format("%12.12s|%12.12s|%12.12s\n", name, price, countStr);
                totalPrice = totalPrice.add(price);
            } else {
                System.out.println("Could not find suitable item " + s + " when making price estimation!");
                return null;
            }
        }
        estimate += String.format("%12.12s-%12.12s-%12.12s\n", 
                "----------------", "-----------------", "-----------------------");
        estimate += String.format("%12.12s %12.12s\n","Yhteensä:",totalPrice);
        estimate += String.format("%12.12s-%12.12s-%12.12s\n",
                "----------------", "-----------------", "-----------------------");
        return estimate;
    }

    public static void showCustomQuery(String query) throws SQLException {
        try {
            Statement getQuery = Global.dbConnection.createStatement();
            ResultSet rs = getQuery.executeQuery(query);
            if(rs.next()){
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

                do {
                    String dataRow = "";
                    for(int i=1; i <= columns; i++){
                        dataRow += String.format("%12.12s| ", rs.getString(i));
                    }
                    System.out.println(dataRow);
                } while (rs.next());
            }
            getQuery.close();
        } catch (SQLException e){
            System.out.println("SQLException while processing this query:");
            System.out.println(query);
            System.out.println("Exception: " + e.getMessage());
        }
    }

    //returns success
    public static boolean addUtilityToWork(int workId, int utilityId, int amount, double discount) throws SQLException {
        boolean success = false;
        Statement getAmount = Global.dbConnection.createStatement();
        ResultSet rs = getAmount.executeQuery(
                "SELECT varastotilanne FROM laskutettava "+
                "WHERE laskutettava_id = " +utilityId);
        rs.next();
        int stock = rs.getInt("varastotilanne");
        if(!rs.wasNull() && stock - amount < 0){
            throw new IllegalArgumentException(
                    "Not enough of utility in stock! " +stock+"-"+amount+"="+ (stock-amount) );
        }

        getAmount.close();

        Statement updateStock = Global.dbConnection.createStatement();
        updateStock.executeUpdate(
                "UPDATE laskutettava "+
                "SET varastotilanne = varastotilanne - "+amount+" "+
                "WHERE laskutettava_id = " + utilityId + " "
                );
        updateStock.close();

        Statement addWorkContent = Global.dbConnection.createStatement();
        addWorkContent.executeUpdate(
                "INSERT INTO sisaltaa VALUES(" +
                String.format("'%s','%s',%d,%f",workId,utilityId,amount,discount) +
                ")"
                );

        return success;
    }

    public static void changeChargeableWares(Scanner userInput, int id) throws SQLException {
        String answer = Utils.askUser("Set wares to: ",userInput);
        Statement st = Global.dbConnection.createStatement();
        st.executeUpdate(
                "UPDATE laskutettava SET varastotilanne = '"+answer+"'"+
                " WHERE laskutettava_id = "+id);
        st.close();
        System.out.println("Chargeable id[" + id + "] wares set to " + answer + ".");
    }

    public static void addCustomer(Scanner userInput ) throws SQLException {
        System.out.println("Adding new user");
        String name = Utils.askUser("Enter name: ",userInput);
        String address = Utils.askUser("Enter address: ",userInput);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "yksityinen",
            "yrittaja"
        ));
        int type = Utils.askSelection("Enter customer type: ", options, userInput);
        if(type==-1) return;
        String values = String.format(
                "DEFAULT,'%s','%s','%s'"
                ,name,address,options.get(type)
                );
        Utils.askInsertion(userInput,"asiakas",values);
    }

    public static void addWorksite(Scanner userInput ) throws SQLException {
        System.out.println("Adding a new worksite");
        String address = Utils.askUser("Enter address: ",userInput);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(
            "kesamokki",
            "asunto",
            "muu"
        ));
        int type = Utils.askSelection("Enter customer type: ", options, userInput);
        if(type==-1) return;
        String values = String.format(
                "DEFAULT,'%s','%s'"
                ,address,options.get(type)
                );
        Utils.askInsertion(userInput,"tyokohde",values);
    }

}
