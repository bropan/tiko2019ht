import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
//THIS WILL CREATE DATABASE SCHEMA AND/OR TABLES IF THEY DON'T EXIST YET
public class DatabaseStructureHandler {

    private DatabaseStructureHandler (){
    }

    public static void createAndSwitchToSchema(Connection con, String schemaName) throws SQLException{
        Statement schemaCheck = con.createStatement();
        ResultSet schemaResult = schemaCheck.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "'");
        boolean foundSchema = schemaResult.next();
        schemaCheck.close();
        
        if(!foundSchema){
            System.out.println("Schema not found, creating schema with name " + schemaName);
            Statement newSchema = con.createStatement();
            newSchema.executeUpdate("CREATE SCHEMA " + schemaName);
            newSchema.close();
        }

        Statement changeSchema = con.createStatement();
        changeSchema.executeUpdate("SET search_path TO " + schemaName);
        changeSchema.close();
    }

    public static void createTypesIfAbsent(Connection con) throws SQLException {
        checkAndCreateType(con,
                "asiakas_tyyppi",
                "'yrittaja','yksityinen'"); 
    }

    //Checks if all tables defined in this method exist and creates them if they don't
    public static void createTablesIfAbsent(Connection con, String schemaName) throws SQLException {
        String tableName = null;

        tableName = "asiakas";
        checkAndCreateTable(con,schemaName,tableName,
        "CREATE TABLE " + tableName + "(        "+
        "    asiakas_id INT,                    "+
        "    nimi VARCHAR(128),                 "+
        "    asiakastyyppi asiakas_tyyppi,      "+
        "    PRIMARY KEY(asiakas_id)            "+
        ")                                      "+
        "");
    }

    private static void checkAndCreateTable(Connection con, String schemaName, String tableName, String content) throws SQLException {	
        try {
            Statement tableCheck = con.createStatement();
            ResultSet tableResult = tableCheck.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = '" + schemaName + "'" +
                    " AND table_name = '" + tableName + "'"
            );
            boolean foundTable = tableResult.next();
            tableCheck.close();

            if(!foundTable){
                System.out.println("Table " + tableName + " not found. Creating it...");
                Statement createTable = con.createStatement();
                createTable.executeUpdate(content);
                createTable.close();
            }
        } catch (SQLException e){
            System.out.println("SQLException when checking/creating table " + tableName + ": " + e.getMessage());
        }
    }



    private static void checkAndCreateType(Connection con, String typeName, String fields) throws SQLException {
        try {
        Statement createType = con.createStatement();
        createType.executeUpdate(
                "DO $$" +
                "BEGIN" +
                "    IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = '"+typeName+"') THEN"+
                "        CREATE TYPE "+typeName+" AS ENUM ("+fields+");" +
                "    END IF;"+
                "END$$;"
                );
        //CREATE TYPE asiakas_tyyppi AS ENUM (’yrittaja’,’yksityinen’); 
        createType.close();
        } catch (SQLException e){
            System.out.println("Exception when checking/creating type " + typeName + ": " + e.getMessage());
            throw e;
        }
    }
}
