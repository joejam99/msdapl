/**
 * NrSeqLookupUtil.java
 * @author Vagisha Sharma
 * Aug 18, 2008
 * @version 1.0
 */
package org.yeastrc.ms.dao.nrseq;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.DAOFactory;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * 
 */
public class NrSeqLookupUtil {

    
private static final Logger log = Logger.getLogger(DAOFactory.class);
    
    // initialize the SqlMapClient
    private static SqlMapClient sqlMap;
    
    static {
        Reader reader = null;
        String ibatisConfigFile = "org/yeastrc/ms/sqlmap/nrseq/NrSeqSqlMapConfig.xml";
        try {
            reader = Resources.getResourceAsReader(ibatisConfigFile);
            sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
        }
        catch (IOException e) {
            log.error("Error reading Ibatis config xml: "+ibatisConfigFile, e);
            throw new RuntimeException("Error reading Ibatis config xml: "+ibatisConfigFile, e);
        }
        catch (Exception e) {
            log.error("Error initializing "+DAOFactory.class.getName()+" class: ", e);
            throw new RuntimeException("Error initializing "+DAOFactory.class.getName()+" class: ", e);
        }
        System.out.println("Loaded Ibatis SQL map config");
    }
    
    
    private NrSeqLookupUtil() {}
    
    /**
     * Returns a match that matches the given accession exactly.
     * @param databaseName
     * @param accession
     * @return
     */
    public static int getProteinId(String databaseName, String accession){
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("dbName", databaseName);
        map.put("accession", accession);
        Integer id = null;
        String statementName = "NrSeq.selectProteinIdForDbNameExactMatch";
        try {
            id = (Integer) sqlMap.queryForObject(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
        if (id == null)
            return 0;
        return id;
    }
    
    /**
     * 
     * @param databaseName
     * @param accession
     * @return
     */
    public static List<Integer> getProteinIdsLikeAccesion(String databaseName, String accession) {
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("dbName", databaseName);
        map.put("accession", accession+"%");
        String statementName = "NrSeq.selectProteinIdForDbName";
        try {
            return sqlMap.queryForList(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
    }
    
    /**
     * Returns a match that matches the given accession exactly.
     * @param databaseId
     * @param accession
     * @return
     */
    public static int getProteinId(int databaseId, String accession) {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("dbId", databaseId);
        map.put("accession", accession);
        Integer id = null;
        String statementName = "NrSeq.selectProteinIdForDbIdExactMatch";
        try {
            id = (Integer) sqlMap.queryForObject(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
        if (id == null)
            return 0;
        return id;
    }
    
    /**
     * 
     * @param databaseId
     * @param accession
     * @return
     */
    public static List<Integer> getProteinIdsPartialAccession(int databaseId, String accession) {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("dbId", databaseId);
        map.put("accession", accession+"%");
        String statementName = "NrSeq.selectProteinIdForDbIdPartialAcc";
        try {
            return sqlMap.queryForList(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
    }
    
    /**
     * 
     * @param databaseId
     * @param accession
     * @return
     */
    public static List<Integer> getProteinIdsForPeptidePartialAccession(int databaseId, String accession, String peptide) {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("dbId", databaseId);
        map.put("accession", accession+"%");
        map.put("sequence", "%"+peptide+"%");
        String statementName = "NrSeq.selectProteinIdForDbIdAndPeptideAndPartialAcc";
        try {
            return sqlMap.queryForList(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
    }
    
    /**
     * 
     * @param databaseId
     * @param accession
     * @return
     */
    public static List<Integer> getProteinIdsForPeptidePartialAccession2(int databaseId, String accession, String peptide) {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("dbId", databaseId);
        map.put("accession", "%"+accession+"%");
        map.put("sequence", "%"+peptide+"%");
        String statementName = "NrSeq.selectProteinIdForDbIdAndPeptideAndPartialAcc";
        try {
            return sqlMap.queryForList(statementName, map);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
    }
    
    /**
     * 
     * @param databaseName
     * @return
     */
    public static int getDatabaseId(String databaseName) {
        String statementName = "NrSeq.selectDatabaseId";
        Integer id = null;
        try {
            id = (Integer) sqlMap.queryForObject(statementName, databaseName);
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
        if (id == null)
            return 0;
        return id;
    }
    
    /**
     * 
     * @param searchDatabaseId
     * @param proteinId
     * @return
     * @throws NrSeqLookupException if no matching database entry is found
     */
    public static String getProteinAccession(int searchDatabaseId, int proteinId) throws NrSeqLookupException {
        String statementName = "NrSeq.selectProteinAccession";
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("databaseId", searchDatabaseId);
        map.put("proteinId", proteinId);
        try {
            String acc = (String) sqlMap.queryForObject(statementName, map);
            if (acc == null)
                throw new NrSeqLookupException(searchDatabaseId, proteinId);
            return acc;
        }
        catch (SQLException e) {
            log.error("Failed to execute select statement: ", e);
            throw new RuntimeException("Failed to execute select statement: "+statementName, e);
        }
    }
}