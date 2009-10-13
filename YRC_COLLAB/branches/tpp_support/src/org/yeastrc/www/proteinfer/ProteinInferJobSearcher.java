package org.yeastrc.www.proteinfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.ProteinferDAOFactory;
import org.yeastrc.ms.dao.analysis.MsSearchAnalysisDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferRunDAO;
import org.yeastrc.ms.dao.search.MsRunSearchDAO;
import org.yeastrc.ms.dao.search.MsSearchDAO;
import org.yeastrc.ms.domain.analysis.MsSearchAnalysis;
import org.yeastrc.ms.domain.protinfer.ProteinInferenceProgram;
import org.yeastrc.ms.domain.protinfer.ProteinferRun;
import org.yeastrc.ms.domain.search.MsSearch;

public class ProteinInferJobSearcher {

    private static final Logger log = Logger.getLogger(ProteinInferJobSearcher.class.getName());
    
    private static final ProteinferDAOFactory factory = ProteinferDAOFactory.instance();
    private static final ProteinferRunDAO runDao = factory.getProteinferRunDao();
    private static final MsSearchDAO searchDao = DAOFactory.instance().getMsSearchDAO();
    private static final MsSearchAnalysisDAO analysisDao = DAOFactory.instance().getMsSearchAnalysisDAO();
    
    private static ProteinInferJobSearcher instance;
    
    private ProteinInferJobSearcher() {}
    
    public static ProteinInferJobSearcher instance() {
        if(instance == null)
            instance = new ProteinInferJobSearcher();
        return instance;
    }
    
    public List<ProteinferJob> getProteinferJobsForMsExperiment(int experimentId) {
        
        List<Integer> pinferRunIds = getProteinferIdsForMsExperiment(experimentId);
        
        if(pinferRunIds == null || pinferRunIds.size() == 0)
            return new ArrayList<ProteinferJob>(0);
        
        List<ProteinferJob> jobs = new ArrayList<ProteinferJob>(pinferRunIds.size());
        for(int pid: pinferRunIds) {
            ProteinferJob job = getJob(pid);
            if(job != null)
                jobs.add(job);
        }
        // sort jobs by id
        Collections.sort(jobs, new Comparator<ProteinferJob>() {
            public int compare(ProteinferJob o1, ProteinferJob o2) {
                return Integer.valueOf(o1.getPinferId()).compareTo(o2.getPinferId());
            }});
        return jobs;
    }
    
    public List<Integer> getProteinferIdsForMsExperiment(int experimentId) {
        
        Set<Integer> pinferRunIds = new HashSet<Integer>();
        
        // Get the searchIds for this experiment
        List<Integer> searchIds = DAOFactory.instance().getMsSearchDAO().getSearchIdsForExperiment(experimentId);
        for(int searchId: searchIds) {
            List<Integer> piRunIds = getPinferRunIdsForSearch(searchId);
            pinferRunIds.addAll(piRunIds);
        }
        
        if(pinferRunIds == null || pinferRunIds.size() == 0)
            return new ArrayList<Integer>(0);
        
        return new ArrayList<Integer>(pinferRunIds);
    }

    public List<ProteinferJob> getProteinferJobsForMsSearch(int msSearchId) {
        
        List<Integer> pinferRunIds = getPinferRunIdsForSearch(msSearchId);
        
        if(pinferRunIds == null || pinferRunIds.size() == 0)
            return new ArrayList<ProteinferJob>(0);
        
        List<ProteinferJob> jobs = new ArrayList<ProteinferJob>(pinferRunIds.size());
        for(int pid: pinferRunIds) {
            ProteinferRun run = runDao.loadProteinferRun(pid);
            if(run != null) {
                
                ProteinferJob job = getJob(run.getId());
                if(job != null)
                    jobs.add(job);
            }
        }
        // sort jobs by id
        Collections.sort(jobs, new Comparator<ProteinferJob>() {
            public int compare(ProteinferJob o1, ProteinferJob o2) {
                return Integer.valueOf(o1.getPinferId()).compareTo(o2.getPinferId());
            }});
        return jobs;
    }


    private List<Integer> getPinferRunIdsForSearch(int msSearchId) {
        
        // load the search
        MsSearch search = searchDao.loadSearch(msSearchId);
        
        Set<Integer> pinferIdsSet = new HashSet<Integer>();
        
        // first get all the runSearchIds for this search
        List<Integer> msRunSearchIds = getRunSearchIdsForMsSearch(msSearchId);
        // load protein inference results for the runSearchIDs where the input generator was 
        // the search program
        List<Integer> searchInputIds = runDao.loadProteinferIdsForInputIds(msRunSearchIds, search.getSearchProgram());
        pinferIdsSet.addAll(searchInputIds);
        
        
        // now check if there is any analysis associated with this search
        List<Integer> analysisIds = getAnalysisIdsForMsSearch(msSearchId);
        
        // get all the runSearchAnalysisIds for each analysis done on the search
        for(int analysisId: analysisIds) {
            // load the analysis
            MsSearchAnalysis analysis = analysisDao.load(analysisId);
            List<Integer> analysisInputIds = runDao.loadProteinferIdsForInputIds(
                    getRunSearchAnalysisIdsForAnalysis(analysisId), analysis.getAnalysisProgram());
            pinferIdsSet.addAll(analysisInputIds);
        }
        
        return new ArrayList<Integer>(pinferIdsSet);
    }
    
    
    /**
     * Returns a ProteinferJob object if the protein inference run with the given id
     * @param pinferRunId
     * @return
     */
    public ProteinferJob getJob(int pinferRunId) {
        
        ProteinferRun run = runDao.loadProteinferRun(pinferRunId);
        
        if(run == null || !ProteinInferenceProgram.isIdPicker(run.getProgram()))
            return null;
        
//      // make sure the input generator for this protein inference program was
//      // a search program or an analysis program
//      if(!Program.isSearchProgram(run.getInputGenerator()) && !Program.isAnalysisProgram(run.getInputGenerator()))
//      continue;
        ProteinferJob job = null;
        try {
            job = getPiJob(run.getId());
        }
        catch (SQLException e) {
            log.error("Exception getting ProteinferJob", e);
            return null;
        }
        if(job == null) {
            log.error("No job found with protein inference run id: "+pinferRunId);
            return null;
        }
        job.setProgram(run.getProgramString());
        job.setVersion(run.getProgramVersion());
        job.setComments(run.getComments());
        job.setDateRun(run.getDate());
        return job;
    }
    
    
    private ProteinferJob getPiJob(int pinferRunId) throws SQLException {
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            
            String sql = "SELECT * FROM tblJobs AS j, tblProteinInferJobs AS pj "+
                        "WHERE j.id = pj.jobID AND pj.piRunID="+pinferRunId;
            
            conn = DBConnectionManager.getConnection(DBConnectionManager.JOB_QUEUE);
            stmt = conn.prepareStatement( sql );
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                ProteinferJob job = new ProteinferJob();
                job.setId( rs.getInt("jobID"));
                job.setSubmitter( rs.getInt( "submitter" ) );
                job.setType( rs.getInt( "type" ) );
                job.setSubmitDate( rs.getDate( "submitDate" ) );
                job.setLastUpdate( rs.getDate( "lastUpdate" ) );
                job.setStatus( rs.getInt( "status" ) );
                job.setAttempts( rs.getInt( "attempts" ) );
                job.setLog( rs.getString( "log" ) );
                job.setPinferRunId(pinferRunId);
                return job;
            }
            
        } finally {
            
            if (rs != null) {
                try { rs.close(); rs = null; } catch (Exception e) { ; }
            }

            if (stmt != null) {
                try { stmt.close(); stmt = null; } catch (Exception e) { ; }
            }
            
            if (conn != null) {
                try { conn.close(); conn = null; } catch (Exception e) { ; }
            }           
        }
        return null;
    }

    private List<Integer> getRunSearchIdsForMsSearch(int msSearchId) {
        org.yeastrc.ms.dao.DAOFactory factory = org.yeastrc.ms.dao.DAOFactory.instance();
        MsRunSearchDAO runSearchDao = factory.getMsRunSearchDAO();
        return runSearchDao.loadRunSearchIdsForSearch(msSearchId);
    }
    
    private List<Integer> getAnalysisIdsForMsSearch(int msSearchId) {
        return analysisDao.getAnalysisIdsForSearch(msSearchId);
    }
    
    private List<Integer> getRunSearchAnalysisIdsForAnalysis(int analysisId) {
        org.yeastrc.ms.dao.DAOFactory factory = org.yeastrc.ms.dao.DAOFactory.instance();
        return factory.getMsRunSearchAnalysisDAO().getRunSearchAnalysisIdsForAnalysis(analysisId);
    }
}
