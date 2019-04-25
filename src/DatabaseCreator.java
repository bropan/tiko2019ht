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
                "'suunnitelma','tarjous','sopimus','valmis'"
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
            "FOREIGN KEY (laskutettava_id) REFERENCES laskutettava(laskutettava_id)     "+
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
            "DEFAULT, 'Esko Meikäläinen', 'Pallopolku 96', 'yksityinen'"  ,
            "DEFAULT, 'Tauno Salonen', 'Kollikuja 999', 'yksityinen'"     ,
            "DEFAULT, 'Jooseppi Virtanen', 'Kiertotie 123', 'yksityinen'" ,
            "DEFAULT, 'Oy Aisankannattaja Ab', 'Kaartotie 7B', 'yrittaja'" ,
            "DEFAULT, 'Penaali Oy', 'Kynätie 7', 'yrittaja'"              ,
            "DEFAULT, 'Mestarifirma Oy', 'Mahtavuudenkatu 2', 'yrittaja'" ,
            "DEFAULT, 'Ari Vatanen', 'Relaatiokatu 78', 'yksityinen'"     ,
            "DEFAULT, 'Heikki Hyyrö', 'Mestarintie 12B', 'yksityinen'"    ,
            "DEFAULT, 'Hwang Dae-du', 'Soulinkuja 17', 'yksityinen'"      ,
            "DEFAULT, 'Laitevalmistaja Oy', 'Mutaojankatu 9', 'yrittaja'" ,
            "DEFAULT, 'Tmi Lukkoseppämies', 'Karalahdentie 2', 'yrittaja'",
            "DEFAULT, 'Superhyperfirma Oy', 'Parhaudentie 1', 'yrittaja'" ,
            "DEFAULT, 'Metsänsahaus Oy', 'Sysimetsä 8', 'yrittaja'"       ,
            "DEFAULT, 'Puunhalaus Oy', 'Hippikatu 420', 'yrittaja'"       ,
            "DEFAULT, 'BetoninValmistus Oy', 'Kivitie 8', 'yrittaja'"     ,
            "DEFAULT, 'Tommi Läntinen', 'Via dolorosa 88', 'yksityinen'"  ,
            "DEFAULT, 'Arttu Wiskari', 'Mökkitie 123', 'yksityinen'"      ,
            
        }; DatabaseStructureHandler.populateTable(
                "asiakas", asiakasValues
        );

        String[] tyokohdeValues = {
            "DEFAULT, 'Peltotie 1', 'asunto'"           ,
            "DEFAULT, 'Metsatie 2', 'kesamokki'"        ,
            "DEFAULT, 'Mokkitie 3', 'kesamokki'"        ,
            "DEFAULT, 'Metsänkeskusta 3', 'muu'"        ,
            "DEFAULT, 'Mokkitie 5', 'muu'"              ,
            "DEFAULT, 'Paalutie 18', 'muu'"             ,
            "DEFAULT, 'Kujatie 13', 'muu'"              ,
            "DEFAULT, 'Tiepolku 333', 'muu'"            ,
            "DEFAULT, 'Pitkänmatkanpää 2', 'asunto'"    ,
            "DEFAULT, 'Kesämökinkatu 33', 'kesamokki'"  ,
            "DEFAULT, 'Kotipihantie 12', 'asunto'"      ,
            "DEFAULT, 'Pihakuja 92', 'muu'"             ,
            "DEFAULT, 'Kärrypolku 5', 'muu'"            ,
            "DEFAULT, 'Lokkitie 123', 'muu'"            ,
            "DEFAULT, 'Poikkitie 55', 'asunto'"         ,
            "DEFAULT, 'Shokkitie 74', 'muu'"            ,
            "DEFAULT, 'Mäkipolku 6', 'kesamokki'"       ,

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
            "DEFAULT, 'sahkojohto', 'm', 'tarvike', 2000, 0.5",
            "DEFAULT, 'lamppu', 'kpl', 'tarvike', 500, 1",
            "DEFAULT, 'loisteputki', 'kpl', 'tarvike', '150', 2",
            "DEFAULT, 'kolmivaihekilowattituntimittari', 'kpl', 'tarvike', 10, 150",

        }; DatabaseStructureHandler.populateTable(
                "laskutettava", laskutettavaValues
        );

        String[] sopimusValues = {
            "DEFAULT, 'tuntipalkka', 'valmis', 1 "  , 
            "DEFAULT, 'urakka', 'tarjous', 2 "      , 
            "DEFAULT, 'urakka', 'suunnitelma', 3 "  ,  
            "DEFAULT, 'tuntipalkka', 'tarjous', 4 "  ,  
            "DEFAULT, 'tuntipalkka', 'tarjous', 5 "  ,  
            "DEFAULT, 'urakka', 'suunnitelma', 6 "  ,  
            "DEFAULT, 'tuntipalkka', 'suunnitelma', 7 "  ,  
            "DEFAULT, 'urakka', 'suunnitelma', 8 "  ,  
            "DEFAULT, 'urakka', 'valmis', 9 "  ,  
            "DEFAULT, 'urakka', 'tarjous', 10 "  ,  
            "DEFAULT, 'urakka', 'valmis', 11 "  ,  
            "DEFAULT, 'urakka', 'suunnitelma', 12 "  ,  
            "DEFAULT, 'urakka', 'valmis', 13 "  ,  
            "DEFAULT, 'urakka', 'suunnitelma', 14 "  ,  
            "DEFAULT, 'urakka', 'suunnitelma', 15 "  ,  
            "DEFAULT, 'urakka', 'tarjous', 16 "  ,  

        }; DatabaseStructureHandler.populateTable(
                "sopimus", sopimusValues
        );

        String[] tyosuoritusValues = {
            "DEFAULT, 1, 1",
            "DEFAULT, 2, 1",
            "DEFAULT, 3, 2",
            "DEFAULT, 5, 3",
            "DEFAULT, 6, 5",
            "DEFAULT, 4, 6",
            "DEFAULT, 8, 4",
            "DEFAULT, 9, 7",
            "DEFAULT, 11, 8",
            "DEFAULT, 10, 10",
            "DEFAULT, 13, 2",
            "DEFAULT, 12, 3",
            "DEFAULT, 14, 4",
            "DEFAULT, 12, 8",
            "DEFAULT, 11, 6",
            "DEFAULT, 11, 4",

        }; DatabaseStructureHandler.populateTable(
                "tyosuoritus", tyosuoritusValues
        );

        String[] sisaltaaValues = {
           "1, 1, 20, 1.0",
           "1, 2, 20, 1.0",
           "1, 3, 2, 1.0",
           "1, 6, 60, 1.0",
           "1, 8, 15, 1.0",

           "2, 1, 100, 0",
           "2, 2, 23, 0",
           "2, 3, 25, 0",

           "3, 3, 65, 1.0",
           "3, 8, 55, 0",
           "3, 6, 12, 1.0",

           "4, 7, 1, 1.0",
           "4, 7, 3, 0",

           "5, 5, 21, 0",
           "5, 4, 22, 0",
           "5, 3, 36, 1.0",

           "6, 5, 100, 0",
           "6, 2, 120, 0",

           "7, 2, 6, 0",
           "7, 3, 44, 0",
           "7, 4, 2, 0",
           "7, 5, 12, 1.0",

           "8, 7, 13, 0",
           "8, 8, 2, 0",

           "9, 6, 3, 0",

           "10, 6, 6, 1.0",
           "10, 3, 8, 0",

        }; DatabaseStructureHandler.populateTable(
                "sisaltaa", sisaltaaValues
        );

        String[] laskuValues = {
            "DEFAULT, 1, 'kertamaksu', 'valmis', '2019-04-23', '2019-05-10', 0, 'maksamatta', NULL, 0, 0, NULL", 
            "DEFAULT, 1, 'osamaksu', 'valmis', '2019-03-30', '2019-05-13', 0, 'maksamatta', NULL, 0, 0, NULL",
            "DEFAULT, 1, 'kertamaksu', 'kesken', '2019-04-15', '2019-05-21', 0, 'maksamatta', NULL, 0, 0, NULL",
            "DEFAULT, 1, 'osamaksu', 'valmis', '2019-04-01', '2019-05-22', 0, 'maksamatta', NULL, 0, 0, NULL",
            "DEFAULT, 1, 'osamaksu', 'kesken', '2019-04-03', '2019-05-16', 0, 'maksamatta', NULL, 0, 0, NULL"
        }; DatabaseStructureHandler.populateTable(
                "lasku", laskuValues
        );

    }
}
