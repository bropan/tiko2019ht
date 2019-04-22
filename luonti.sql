CREATE TYPE maksun_tila AS ENUM ('maksamatta', ’muistutettu’, 'perinnassa', 'maksettu'); 
CREATE TYPE asiakas_tyyppi AS ENUM (’yrittaja’,’yksityinen’); 
CREATE TYPE tyokohde_tyyppi AS ENUM (‘kesamokki’,’asunto’,’muu’); 
CREATE TYPE sopimus_tyyppi AS ENUM (’tuntipalkka’,’urakka’); 
CREATE TYPE lasku_tyyppi AS ENUM (‘kertamaksu’,’osamaksu’); 
CREATE TYPE lasku_tila AS ENUM (‘kesken’,’valmis’); 
CREATE TYPE lasku_maksun_tila AS ENUM (‘maksamatta’,’maksettu’); 
CREATE TYPE laskutettava_tyyppi AS ENUM (‘tyo’,’tarvike’); 

 

CREATE TABLE asiakas ( 
    asiakas_id INT, 
    nimi VARCHAR(128), 
    asiakastyyppi asiakas_tyyppi, 
    PRIMARY KEY(asiakas_id) 
); 

CREATE TABLE laskutettava ( 
    laskutettava_id INT, 
    nimi VARCHAR(256), 
    yksikko VARCHAR(64), 
    tyyppi laskutettava_tyyppi, 
    varastotilanne INT, 
    sisaanostohinta NUMERIC(2), 
    PRIMARY KEY(tarvike_id) 
); 

CREATE TABLE tyokohde ( 
    kohde_id INT, 
    osoite VARCHAR(256), 
    kohdetyyppi tyokohde_tyyppi, 
    PRIMARY KEY(kohde_id) 
); 

CREATE TABLE sopimus ( 
    sopimus_id INT, 
    sopimustyyppi sopimus_tyyppi, 
    asiakas_id INT, 
    FOREIGN KEY asiakas_id REFERENCES asiakas(asiakas_id), 
    PRIMARY KEY(sopimus_id) 
); 

Sivunvaihto 

CREATE TABLE sisaltaa ( 
    tyosuoritus_id INT, 
    laskutettava_id INT, 
    lkm INT, 
    alennus NUMERIC, 
    FOREIGN KEY tyosuoritus_id REFERENCES tyosuoritus(tyosuoritus_id), 
    FOREIGN KEY laskutettava_id REFERENCES laskutettava(laskutettava_id), 
    PRIMARY KEY (tyosuoritus_id,tarvike_id) 
); 

CREATE TABLE lasku ( 
    lasku_id INT, 
    sopimus_id INT, 
    laskutyyppi lasku_tyyppi, 
    laskun_tila lasku_tila, 
    laskutuspvm DATE, 
    erapvm DATE, 
    laskutuskerta INT, 
    maksun_tila lasku_maksun_tila, 
    maksupvm DATE, 
    era INT, 
    erat INT, 
    FOREIGN KEY sopimus_id REFERENCES sopimus(sopimus_id), 
    FOREIGN KEY asiakas_id REFERENCES sopimus(asiakas_id), 
    PRIMARY KEY(lasku_id) 
); 
