import java.sql.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

    public static final String LOGIN_CONFIG_FILE_PATH = "loginconfig.txt";
    public static final String GLOBAL_SCHEMA = "sahkofirma";
    public static final String GLOBAL_SCHEMA_OWNER = "tiko2019r18";

    public LoginCredentials GLOBAL_loginCredentials = null;

    public static void main(String args[]) {
		Scanner s = new Scanner(System.in);
        Connection con = null;
        
        System.out.println();
        System.out.println("-------------------------------------------------------------------");
        System.out.println("--- Welcome to Sähkövirma Sähkötärsky ooyy laskutusjärjestelmäm ---");
        System.out.println("-------------------------------------------------------------------");
        System.out.println();
        try {
            //Establishes connection, inits schema,tables,values
            Init.init(s);

            CLI.run(s);



        } catch (SQLException e) {
            System.out.println("Caught unhandled SQL exception in Main: " + e.getMessage());
            e.printStackTrace();
            if(con != null) try {
                System.out.println("Rolling back!");
                con.rollback();
            } catch (SQLException e2){
                System.out.println("Rollback failed! " + e2.getMessage());
            }
			System.out.println("Aborting due to unhandled SQL exception.");
        } catch (Exception e) {
            System.out.println("Unhandled exception in Main: " + e.getMessage());
            e.printStackTrace();
            if(con != null) try {
                System.out.println("Rolling back!");
                con.rollback();
            } catch (SQLException e2){
                System.out.println("Rollback failed! " + e2.getMessage());
            }
			System.out.println("Aborting due to unhandled exception.");
        }            
 
        if (con != null) try {
            con.close();
        } catch(SQLException e) {
            System.out.println("Closing the database connection failed. " + e.getMessage());
            return;
        }

        System.out.println("Exitting program. Goodbye!");
    }

}
