import java.sql.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

    public static final String LOGIN_CONFIG_FILE_PATH = "loginconfig.txt";

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
            con = Init.init(s);

            CLI.run(s);



        } catch (SQLException e) {
            System.out.println("Caught unhandled SQL exception: " + e.getMessage());
			System.out.println("Aborting due to unhandled SQL exception.");
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
