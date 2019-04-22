import java.sql.Connection;
import java.sql.SQLException;
//For containing the type,table creation SQL
public class DatabaseCreator {

    private DatabaseCreator(){}

    public static void createTypesIfAbsent(Connection con) throws SQLException {
        System.out.println("Checking that all required types exist...");
        DatabaseStructureHandler.checkAndCreateType(con,
            "asiakas_tyyppi",
                "'yrittaja','yksityinen'"
        ); 
        DatabaseStructureHandler.checkAndCreateType(con,
            "maksun_tila",
                "'maksamatta', 'muistutettu', 'perinnassa', 'maksettu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "asiakas_tyyppi",
                "'yrittaja','yksityinen'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "tyokohde_tyyppi",
                "'kesamokki','asunto','muu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "sopimus_tyyppi",
                "'tuntipalkka','urakka'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "sopimus_tila",
                "'suunnitelma','tarjous','sopimus'"
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "lasku_tyyppi",
                "'kertamaksu','osamaksu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "lasku_tila",
                "'kesken','valmis'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "lasku_maksun_tila",
                "'maksamatta','maksettu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(con,
            "laskutettava_tyyppi",
                "'tyo','tarvike'" 
        );
    }

    public static void createTablesIfAbsent(Connection con, String schemaName) throws SQLException {
        System.out.println("Checking that all required tables exist...");
        String tableName = null;

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName,
                "asiakas",
        "    asiakas_id SERIAL,                 "+
        "    nimi VARCHAR(128),                 "+
        "    asiakastyyppi asiakas_tyyppi,      "+
        "    PRIMARY KEY(asiakas_id)            "+
        ""
        );

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "laskutettava",
            "laskutettava_id SERIAL,            "+
            "nimi VARCHAR(256),                 "+
            "yksikko VARCHAR(64),               "+
            "tyyppi laskutettava_tyyppi,        "+
            "varastotilanne INT,                "+
            "sisaanostohinta NUMERIC(2),        "+
            "PRIMARY KEY(laskutettava_id)       "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "tyokohde",
            "kohde_id SERIAL,                   "+
            "osoite VARCHAR(256),               "+
            "kohdetyyppi tyokohde_tyyppi,       "+
            "PRIMARY KEY(kohde_id)              "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "sopimus",
            "sopimus_id SERIAL,                                         "+
            "sopimustyyppi sopimus_tyyppi,                              "+
            "tila sopimus_tila,                                         "+
            "asiakas_id INT,                                            "+
            "FOREIGN KEY (asiakas_id) REFERENCES asiakas(asiakas_id),   "+
            "PRIMARY KEY(sopimus_id)                                    "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "tyosuoritus",
            "tyosuoritus_id SERIAL,                                     "+
            "kohde_id INT,                                              "+
            "sopimus_id INT,                                            "+
            "FOREIGN KEY (kohde_id) REFERENCES tyokohde(kohde_id),      "+
            "FOREIGN KEY (sopimus_id) REFERENCES sopimus(sopimus_id),   "+
            "PRIMARY KEY (tyosuoritus_id)                               "+
            ""
        );

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "sisaltaa",
            "tyosuoritus_id INT,                                                        "+
            "laskutettava_id INT,                                                       "+
            "lkm INT,                                                                   "+
            "alennus NUMERIC,                                                           "+
            "FOREIGN KEY (tyosuoritus_id) REFERENCES tyosuoritus(tyosuoritus_id),       "+ 
            "FOREIGN KEY (laskutettava_id) REFERENCES laskutettava(laskutettava_id),    "+
            "PRIMARY KEY (tyosuoritus_id,laskutettava_id)                               "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "lasku",
            "lasku_id SERIAL,                                                       "+
            "sopimus_id INT,                                                        "+
            "laskutyyppi lasku_tyyppi,                                              "+
            "laskun_tila lasku_tila,                                                "+
            "laskutuspvm DATE,                                                      "+
            "erapvm DATE,                                                           "+
            "laskutuskerta INT,                                                     "+
            "maksun_tila lasku_maksun_tila,                                         "+
            "maksupvm DATE,                                                         "+
            "era INT,                                                               "+
            "erat INT,                                                              "+
            "FOREIGN KEY (sopimus_id) REFERENCES sopimus(sopimus_id),               "+
            "PRIMARY KEY(lasku_id)                                                  "+
            ""
        ); 
    }

    public static void createContentIfAbsent(Connection con) throws SQLException {

        System.out.println("Populating empty tables...");

        String[] asiakasValues = {
            "1, 'Matti Virtanen','yksityinen'"    ,
            "2, 'Esko Meikäläinen ','yksityinen'" ,
            "3, 'Tauno Salonen ','yksityinen'"    ,
        }; DatabaseStructureHandler.populateTable(con,
                "asiakas", asiakasValues
        );

        String[] tyokohdeValues = {
            "1, 'peltotie 1', 'asunto'"           ,
            "2, 'metsatie 2', 'kesamokki'"        ,
            "3, 'mokkitie 3', 'kesamokki'"        ,
        }; DatabaseStructureHandler.populateTable(con,
                "tyokohde", tyokohdeValues
        );

        String[] laskutettavaValues = {
            "1, 'tyo', 'h', 'tyo', NULL, 15",
            "2, 'pistorasia', 'kpl', 'tarvike', 100, 5",
            "3, 'sahkojohto', 'm', 'tarvike', 1000, 1",
        }; DatabaseStructureHandler.populateTable(con,
                "laskutettava", laskutettavaValues
        );

        String[] sopimusValues = {
            "1, 'tuntipalkka', 'sopimus', 1 "  , 
            "2, 'urakka', 'tarjous', 2 "      , 
            "3, 'urakka', 'suunnitelma', 3 "  ,  
        }; DatabaseStructureHandler.populateTable(con,
                "sopimus", sopimusValues
        );

        String[] tyosuoritusValues = {
            "1, 1, 1",
            "2, 1, 1",
            "3, 1, 1",
        }; DatabaseStructureHandler.populateTable(con,
                "tyosuoritus", tyosuoritusValues
        );

        String[] sisaltaaValues = {
           "1, 1, 5, 1.0",
           "1, 2, 2, 1.0",
           "1, 3, 10, 1.0",

           "2, 1, 6, 1.0",
           "2, 2, 1, 1.0",
           "2, 3, 8, 1.0",

           "3, 1, 5, 1.0",
           "3, 2, 3, 1.0",
           "3, 3, 7, 1.0",
        }; DatabaseStructureHandler.populateTable(con,
                "sisaltaa", sisaltaaValues
        );

        String[] laskuValues = {
            "1, 1, 'kertamaksu', 'kesken', NULL, NULL, 0, 'maksamatta', NULL, 0, 0", 
        }; DatabaseStructureHandler.populateTable(con,
                "lasku", laskuValues
        );

    }
}
