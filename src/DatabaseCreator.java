import java.sql.Connection;
import java.sql.SQLException;
//For containing the type,table creation SQL
public class DatabaseCreator {

    private DatabaseCreator(){}

    public static void createTypesIfAbsent() throws SQLException {
        System.out.println("Checking that all required types exist...");
        DatabaseStructureHandler.checkAndCreateType(
            "asiakas_tyyppi",
                "'yrittaja','yksityinen'"
        ); 
        DatabaseStructureHandler.checkAndCreateType(
            "maksun_tila",
                "'maksamatta', 'muistutettu', 'perinnassa', 'maksettu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "asiakas_tyyppi",
                "'yrittaja','yksityinen'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "tyokohde_tyyppi",
                "'kesamokki','asunto','muu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "sopimus_tyyppi",
                "'tuntipalkka','urakka'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "sopimus_tila",
                "'suunnitelma','tarjous','sopimus'"
        );
        DatabaseStructureHandler.checkAndCreateType(
            "lasku_tyyppi",
                "'kertamaksu','osamaksu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "lasku_tila",
                "'kesken','valmis'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "lasku_maksun_tila",
                "'maksamatta','maksettu'" 
        );
        DatabaseStructureHandler.checkAndCreateType(
            "laskutettava_tyyppi",
                "'tyo','tarvike'" 
        );
    }

    public static void createTablesIfAbsent(String schemaName) throws SQLException {
        System.out.println("Checking that all required tables exist...");
        String tableName = null;

        DatabaseStructureHandler.checkAndCreateTable(schemaName,
                "asiakas",
        "    asiakas_id SERIAL,                 "+
        "    nimi VARCHAR(128),                 "+
        "    osoite VARCHAR(256),               "+
        "    asiakastyyppi asiakas_tyyppi,      "+
        "    PRIMARY KEY(asiakas_id)            "+
        ""
        );

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
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

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
                "tyokohde",
            "kohde_id SERIAL,                   "+
            "osoite VARCHAR(256),               "+
            "kohdetyyppi tyokohde_tyyppi,       "+
            "PRIMARY KEY(kohde_id)              "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
                "sopimus",
            "sopimus_id SERIAL,                                         "+
            "sopimustyyppi sopimus_tyyppi,                              "+
            "tila sopimus_tila,                                         "+
            "asiakas_id INT,                                            "+
            "FOREIGN KEY (asiakas_id) REFERENCES asiakas(asiakas_id),   "+
            "PRIMARY KEY(sopimus_id)                                    "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
                "tyosuoritus",
            "tyosuoritus_id SERIAL,                                     "+
            "kohde_id INT,                                              "+
            "sopimus_id INT,                                            "+
            "FOREIGN KEY (kohde_id) REFERENCES tyokohde(kohde_id),      "+
            "FOREIGN KEY (sopimus_id) REFERENCES sopimus(sopimus_id),   "+
            "PRIMARY KEY (tyosuoritus_id)                               "+
            ""
        );

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
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

        DatabaseStructureHandler.checkAndCreateTable(schemaName, 
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
            "edellinen INT,                                                         "+
            "FOREIGN KEY (sopimus_id) REFERENCES sopimus(sopimus_id),               "+
            "PRIMARY KEY(lasku_id)                                                  "+
            ""
        ); 
    }

    public static void createContentIfAbsent() throws SQLException {

        System.out.println("Populating empty tables...");

        String[] asiakasValues = {
            "DEFAULT, 'Matti Virtanen', 'Pensselitie 2', 'yksityinen'"    ,
            "DEFAULT, 'Esko Meikäläinen', 'Pallopolku 96', 'yksityinen'" ,
            "DEFAULT, 'Tauno Salonen', 'Kollikuja 999', 'yksityinen'"    ,
        }; DatabaseStructureHandler.populateTable(
                "asiakas", asiakasValues
        );

        String[] tyokohdeValues = {
            "DEFAULT, 'peltotie 1', 'asunto'"           ,
            "DEFAULT, 'metsatie 2', 'kesamokki'"        ,
            "DEFAULT, 'mokkitie 3', 'kesamokki'"        ,
        }; DatabaseStructureHandler.populateTable(
                "tyokohde", tyokohdeValues
        );

        String[] laskutettavaValues = {
            "DEFAULT, 'asennus', 'h', 'tyo', NULL, 15",
            "DEFAULT, 'suunnittelu', 'h', 'tyo', NULL, 10",
            "DEFAULT, 'pistorasia', 'kpl', 'tarvike', 100, 5",
            "DEFAULT, 'pistorasia', 'kpl', 'tarvike', 100, 10",
            "DEFAULT, 'pistorasia', 'kpl', 'tarvike', 0, 3",
            "DEFAULT, 'sahkojohto', 'm', 'tarvike', 1000, 1",
        }; DatabaseStructureHandler.populateTable(
                "laskutettava", laskutettavaValues
        );

        String[] sopimusValues = {
            "DEFAULT, 'tuntipalkka', 'sopimus', 1 "  , 
            "DEFAULT, 'urakka', 'tarjous', 2 "      , 
            "DEFAULT, 'urakka', 'suunnitelma', 3 "  ,  
        }; DatabaseStructureHandler.populateTable(
                "sopimus", sopimusValues
        );

        String[] tyosuoritusValues = {
            "DEFAULT, 1, 1",
            "DEFAULT, 1, 1",
            "DEFAULT, 1, 1",
        }; DatabaseStructureHandler.populateTable(
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
        }; DatabaseStructureHandler.populateTable(
                "sisaltaa", sisaltaaValues
        );

        String[] laskuValues = {
            "DEFAULT, 1, 'kertamaksu', 'kesken', NULL, NULL, 0, 'maksamatta', NULL, 0, 0", 
        }; DatabaseStructureHandler.populateTable(
                "lasku", laskuValues
        );

    }
}
