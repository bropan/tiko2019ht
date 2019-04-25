public class LongSQLQueryFunctions {

    public static String customerByBillId(int billId){
        return

            "SELECT a.asiakas_id, a.nimi, a.osoite "+
            "FROM lasku as l "+
            "INNER JOIN "+
            "sopimus as s "+
            "ON l.sopimus_id = s.sopimus_id "+
            "INNER JOIN "+
            "asiakas as a "+
            "ON s.asiakas_id = a.asiakas_id "+
            "WHERE lasku_id = "+billId+" "
            ;
    }
    public static String chargeablesByBillId(int billId, String type){
        return
            " SELECT l.lasku_id, sp.sopimus_id, sub.laskutettava_id, sub.nimi, sub.lkm, sub.hinta, sub.tyyppi, sub.yksikko "+
            " FROM lasku AS l "+
            " INNER JOIN sopimus AS sp "+
            " ON sp.sopimus_id = l.sopimus_id "+
            " INNER JOIN "+
            " ( "+
            "     select ts.sopimus_id, lt.laskutettava_id, lt.nimi, SUM(lkm) AS lkm, SUM(lkm)*sisaanostohinta AS hinta, lt.tyyppi ,lt.yksikko"+
            "     FROM tyosuoritus AS ts "+
            "     INNER JOIN sisaltaa AS ss "+
            "     ON ss.tyosuoritus_id = ts.tyosuoritus_id "+
            "     INNER JOIN "+
            "     laskutettava AS lt "+
            "     ON ss.laskutettava_id = lt.laskutettava_id "+
            "     GROUP BY ts.sopimus_id, lt.laskutettava_id, lt.nimi "+
            " ) AS sub "+
            " ON sub.sopimus_id = sp.sopimus_id "+
            " WHERE l.lasku_id = "+billId+" AND sub.tyyppi = '"+type+"'"+
            " ORDER BY hinta "
            ;
    }

    public static String householdWork(int billId){
        return
            "SELECT SUM(sub.hinta) as hinta " + 
            "FROM lasku AS l "+
            "INNER JOIN sopimus AS sp "+
            "ON sp.sopimus_id = l.sopimus_id "+
            "INNER JOIN "+
            "( "+
            "    select  ts.sopimus_id,  "+
            "            lt.laskutettava_id,  "+
            "            lt.nimi, SUM(lkm) AS lkm,  "+
            "            SUM(lkm)*sisaanostohinta AS hinta,  "+
            "            lt.tyyppi,  "+
            "            tk.kohde_id, "+
            "            tk.kohdetyyppi "+
            "    FROM tyosuoritus AS ts "+
            "    INNER JOIN sisaltaa AS ss "+
            "    ON ss.tyosuoritus_id = ts.tyosuoritus_id "+
            "    INNER JOIN laskutettava AS lt "+
            "    ON ss.laskutettava_id = lt.laskutettava_id "+
            "    INNER JOIN tyokohde AS tk "+
            "    ON tk.kohde_id = ts.kohde_id "+
            "    WHERE   tk.kohdetyyppi = 'asunto'  "+
            "            OR "+
            "            tk.kohdetyyppi = 'kesamokki' "+
            "    GROUP BY    ts.sopimus_id,  "+
            "                lt.laskutettava_id,  "+
            "                lt.nimi,  "+
            "                tk.kohde_id,  "+
            "                tk.kohdetyyppi "+
            ") AS sub "+
            "ON sub.sopimus_id = sp.sopimus_id "+
            "WHERE l.lasku_id = "+billId+" AND sub.tyyppi = 'tyo' "+
            "ORDER BY hinta "
            ;
    }

}
