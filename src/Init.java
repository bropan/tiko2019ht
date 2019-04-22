import java.util.Scanner;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
//Initialization utils class. Provides static functions
//for establishing a database connection and getting
//user credentials.
public class Init {    

    //OVERRIDES
    private static final String PROTOCOL = "jdbc:postgresql:";
    private static final String SERVER = "dbstud2.sis.uta.fi";
    private static final int PORT = 5432;
    private static final String DATABASE = "tiko2019r18";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private static final String INIT_SEPARATOR_START = "--------------------" + " INIT_START " + "--------------------" ;
    private static final String INIT_SEPARATOR_END = "--------------------" + " INIT_END " +  "--------------------" + "\n";

    private Init(){
    }

    public static Connection init(Scanner userInput) throws SQLException{        
        Connection con = null;
        boolean attemptingConnect = true;
        boolean lastTimeFailed = false;

        while(attemptingConnect){
            System.out.println(INIT_SEPARATOR_START);
            System.out.println("Initializing...");
            LoginCredentials lc = null;
            try {
                System.out.println("Reading user credentials...");
                lc = getCredentials(userInput,lastTimeFailed);



                String username = lc.username;
                String password = lc.password;

                String connectionTarget = PROTOCOL+"//"+SERVER+":"+ PORT+"/"+DATABASE;
                System.out.println("Establishing connection to: " + connectionTarget);
                con = DriverManager.getConnection(connectionTarget,username,password);
            } catch (SQLException e){
                System.out.println("SQLException occurred: " + e.getMessage());
                lastTimeFailed = true;
                System.out.println("Retrying initialization...");
                System.out.println(INIT_SEPARATOR_END);
            }

            if(con != null){
                attemptingConnect = false;
                System.out.println("Connected succesfully.");
            }
        }

        try {
            con.setAutoCommit(false);
            DatabaseStructureHandler.initDefaultStructure(con,Main.GLOBAL_SCHEMA);
            con.commit();
        } catch (SQLException e){
            System.out.println("Exception in database structure checking/creation: " + e.getMessage());
            con.rollback();
            System.out.println("Rolled back!");
            throw e;
        } finally {
            con.setAutoCommit(true);
        }

        System.out.println(INIT_SEPARATOR_END);
        return con;
    }

    private static LoginCredentials getCredentials(Scanner userInput, boolean lastTimeFailed){
            LoginCredentials lc = null;
            if(!lastTimeFailed){
                lc = loginFromOverride();
                if(lc == null){
                    System.out.println( "Reading login credentials from '" + Main.LOGIN_CONFIG_FILE_PATH + "'...");
                    lc = readLoginFromFile(Main.LOGIN_CONFIG_FILE_PATH);
                    if(lc != null){
                        System.out.println("Login credentials read from '" + Main.LOGIN_CONFIG_FILE_PATH + "'.");
                    } else {
                        System.out.println("Login config file read failed.");
                        System.out.println();
                        System.out.println("TIP: You can create a file called '" + Main.LOGIN_CONFIG_FILE_PATH + "'");
                        System.out.println("and place your login credentials there f.e:");
                        System.out.println("username;Alice");
                        System.out.println("password;p4ssw0rd1234");
                        System.out.println("Or you can use the login creation command in the interface.");
                        System.out.println();

                    }
                } else {
                    System.out.println("Login credentials read from source code overrides.");
                }
            }
            if(lc == null){
                System.out.println("Reading credentials manually from user...");
                lc = CLI.loginFromUserInput(userInput);
            }
            return lc;
    }

    private static LoginCredentials loginFromOverride(){
        LoginCredentials lc = null;
        if(!Utils.isNullOrEmpty(USERNAME) && !Utils.isNullOrEmpty(PASSWORD)){
            return new LoginCredentials(USERNAME,PASSWORD);
        }
        return lc;
    }

    private static LoginCredentials readLoginFromFile(String loginConfFilePath){
        String username = null;
        String password = null;
        LoginCredentials lc = null;
        try ( Scanner fileInput = new Scanner(new File(loginConfFilePath)) ){
            while(fileInput.hasNextLine()){
                String row = fileInput.nextLine();
                String[] fields = row.split(";");
                switch(fields[0]){
                    case "username":
                        username = fields[1];
                    case "password":
                        password = fields[1];
                }
            }
            if(username != null && password != null){
                lc = new LoginCredentials(username,password);
            }
        } catch (Exception e){
            System.out.println(
                    "Catched unhandled exception while reading login credentials: " 
                    + e.getMessage());
        }
        return lc;
    }




}
