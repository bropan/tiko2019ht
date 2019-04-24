import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
//Provides methods for altering database structure i.e adding tables or types
//Actual database creation is in DatabaseCreator.java
public abstract class DatabaseStructureHandler {

    private DatabaseStructureHandler (){}

    public static void initDefaultStructure(String schemaName) throws SQLException {
        System.out.println("Managing default database structure...");

        DatabaseStructureHandler.createAndSwitchToSchema(schemaName);
        DatabaseStructureHandler.createTypesIfAbsent();
        DatabaseStructureHandler.createTablesIfAbsent(schemaName);
        DatabaseStructureHandler.createContentIfAbsent();

        System.out.println("Database default structure initialized. Types, tables and values have been assured/created.");
    }

    public static void createAndSwitchToSchema(String schemaName) throws SQLException{

        System.out.println("Setting role to " + Main.GLOBAL_SCHEMA_OWNER);
        Statement setRole = Global.dbConnection.createStatement();
        setRole.executeUpdate("SET ROLE " + Main.GLOBAL_SCHEMA_OWNER);
        setRole.close();

        Statement schemaCheck = Global.dbConnection.createStatement();
        ResultSet schemaResult = schemaCheck.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "'");
        boolean foundSchema = schemaResult.next();
        schemaCheck.close();
        
        if(!foundSchema){
            System.out.println("Schema not found, creating schema with name " + schemaName);
            Statement newSchema = Global.dbConnection.createStatement();
            newSchema.executeUpdate(
                    "CREATE SCHEMA " + schemaName + " AUTHORIZATION " + Main.GLOBAL_SCHEMA_OWNER);
            newSchema.close();
        }

        Statement changeSchema = Global.dbConnection.createStatement();
        changeSchema.executeUpdate("SET search_path TO " + schemaName);
        changeSchema.close();
    }

    //Removes everything and sets database to default structure
    public static void resetDatabase(String schemaName) throws SQLException{
        System.out.println("Resetting database!");
        Statement removeSchema = Global.dbConnection.createStatement();
        removeSchema.executeUpdate("DROP SCHEMA "+schemaName+" CASCADE");
        removeSchema.close();
        
        initDefaultStructure(schemaName);
    }

    //Creates ENUM types required by the database if they don't exist
    public static void createTypesIfAbsent()throws SQLException {
        DatabaseCreator.createTypesIfAbsent();
    }

    //Checks if all tables defined in this method exist and creates them if they don't
    public static void createTablesIfAbsent(String schemaName) throws SQLException {
        DatabaseCreator.createTablesIfAbsent(schemaName);
    }

    public static void checkAndCreateTable(String schemaName, String tableName, String content) throws SQLException {	
        try {
            Statement tableCheck = Global.dbConnection.createStatement();
            ResultSet tableResult = tableCheck.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = '" + schemaName + "'" +
                    " AND table_name = '" + tableName + "'"
            );
            boolean foundTable = tableResult.next();
            tableCheck.close();

            if(!foundTable){
                System.out.println("Table " + tableName + " not found. Creating it...");
                Statement createTable = Global.dbConnection.createStatement();
                createTable.executeUpdate("CREATE TABLE "+tableName+" ("+content+")");
                createTable.close();
            }
        } catch (SQLException e){
            System.out.println("SQLException when checking/creating table " + tableName + ": " + e.getMessage());
            throw e;
        }
    }



    public static void checkAndCreateType(String typeName, String fields) throws SQLException {
        try {
        Statement createType = Global.dbConnection.createStatement();
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

    public static void createContentIfAbsent()throws SQLException {
        DatabaseCreator.createContentIfAbsent();
    } 

    public static void populateTable(String tableName, String[] values) throws SQLException {
        try {
            Statement contentCheck = Global.dbConnection.createStatement();
            ResultSet contentResult = contentCheck.executeQuery("SELECT * FROM " + tableName);
            boolean hasContent = contentResult.next();
            contentCheck.close();

            if(!hasContent){
                System.out.println("Table "+tableName+" is empty. Adding default values.");
                for(String val : values){
                    Statement addContent = Global.dbConnection.createStatement();
                    addContent.executeUpdate("INSERT INTO "+tableName+" VALUES ("+val+")");
                    addContent.close();
                }
            }
        } catch (SQLException e){
            System.out.println("SQLException when checking/populating table " + tableName + ": " + e.getMessage());
            throw e;
        }
    }
}
