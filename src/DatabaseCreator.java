import java.sql.Connection;
import java.sql.SQLException;
//For containing the type,table creation SQL
public class DatabaseCreator {

    private DatabaseCreator(){}

    public static void createTypesIfAbsent(Connection con) throws SQLException {
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
        String tableName = null;

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName,
                "asiakas",
        "    asiakas_id INT,                    "+
        "    nimi VARCHAR(128),                 "+
        "    asiakastyyppi asiakas_tyyppi,      "+
        "    PRIMARY KEY(asiakas_id)            "+
        ""
        );

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "laskutettava",
            "laskutettava_id INT,               "+
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
            "kohde_id INT,                      "+
            "osoite VARCHAR(256),               "+
            "kohdetyyppi tyokohde_tyyppi,       "+
            "PRIMARY KEY(kohde_id)              "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "sopimus",
            "sopimus_id INT,                                            "+
            "sopimustyyppi sopimus_tyyppi,                              "+
            "tila sopimus_tila,                                         "+
            "asiakas_id INT,                                            "+
            "FOREIGN KEY (asiakas_id) REFERENCES asiakas(asiakas_id),   "+
            "PRIMARY KEY(sopimus_id)                                    "+
            ""
        ); 

        DatabaseStructureHandler.checkAndCreateTable(con,schemaName, 
                "tyosuoritus",
            "tyosuoritus_id INT,                                        "+
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
            "lasku_id INT,                                                          "+
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

}
