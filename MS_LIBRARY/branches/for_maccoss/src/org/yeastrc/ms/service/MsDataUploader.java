package org.yeastrc.ms.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.general.MsExperimentDAO;
import org.yeastrc.ms.dao.run.MsRunDAO;
import org.yeastrc.ms.dao.run.MsScanDAO;
import org.yeastrc.ms.dao.search.MsSearchDAO;
import org.yeastrc.ms.domain.general.MsExperiment;
import org.yeastrc.ms.domain.search.MsSearch;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.service.UploadException.ERROR_CODE;

public class MsDataUploader {

    private static final Logger log = Logger.getLogger(MsDataUploader.class);
    
    private int uploadedSearchId;
    private int uploadedExptId;
    private List<UploadException> uploadExceptionList = new ArrayList<UploadException>();
    
    private String remoteServer;
    private String rawDataDirectory;
    private String remoteRawDataDirectory;
    private String searchDirectory;
    private String remoteSearchDataDirectory;
    private java.util.Date searchDate;
    private String analysisDirectory;
    private boolean doScanChargeMassCheck = false; // For MacCoss lab data
    
    private boolean uploadSearch = false;
    private boolean uploadAnalysis = false;
    
    
    private static final Pattern fileNamePattern = Pattern.compile("(\\S+)\\.(\\d+)\\.(\\d+)\\.(\\d{1})");
    
    private void resetUploader() {
        uploadExceptionList.clear();
        uploadedSearchId = 0;
        uploadedExptId = 0;
    }
    
    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }

    public void setRawDataDirectory(String rawDataDirectory) {
        this.rawDataDirectory = rawDataDirectory;
    }

    public void setRemoteRawDataDirectory(String remoteRawDataDirectory) {
        this.remoteRawDataDirectory = remoteRawDataDirectory;
    }

    public void setSearchDirectory(String searchDirectory) {
        this.searchDirectory = searchDirectory;
        if(searchDirectory != null)
            this.uploadSearch = true;
    }

    public void setRemoteSearchDataDirectory(String remoteSearchDataDirectory) {
        this.remoteSearchDataDirectory = remoteSearchDataDirectory;
    }

    public void setSearchDate(java.util.Date searchDate) {
        this.searchDate = searchDate;
    }

    public void setAnalysisDirectory(String analysisDirectory) {
        this.analysisDirectory = analysisDirectory;
        if(analysisDirectory != null)
            this.uploadAnalysis = true;
    }
    
    public void checkResultChargeMass(boolean doScanChargeMassCheck) {
        this.doScanChargeMassCheck = doScanChargeMassCheck;
    }
    
    public List<UploadException> getUploadExceptionList() {
        return this.uploadExceptionList;
    }
    
    public String getUploadWarnings() {
        StringBuilder buf = new StringBuilder();
        for (UploadException e: uploadExceptionList) {
            buf.append(e.getMessage()+"\n");
        }
        return buf.toString();
    }
    
    public int getUploadedSearchId() {
        return this.uploadedSearchId;
    }
    
    public int getUploadedExperimentId() {
        return this.uploadedExptId;
    }
    
    /**
     * @param remoteServer
     * @param remoteDirectory
     * @param fileDirectory
     * @return database id for experiment if it was uploaded successfully, 0 otherwise
     * @throws UploadException 
     */
    public void uploadData() {

        resetUploader();
        
        log.info("INITIALIZING EXPERIMENT UPLOAD"+
                "\n\tTime: "+(new Date().toString()));
        
        
        // ----- INITIALIZE THE EXPERIMENT UPLOADER
        MsExperimentUploader exptUploader = null;
        try {
            exptUploader = initializeExperimentUploader();
        }
        catch (UploadException e) {
            uploadExceptionList.add(e);
            log.error(e.getMessage(), e);
            return;
        }
        
        // ----- CHECKS BEFORE BEGINNING UPLOAD -----
        log.info("Starting pre-upload checks..");
        if(!exptUploader.preUploadCheckPassed()) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage(exptUploader.getPreUploadCheckMsg());
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        log.info(exptUploader.getPreUploadCheckMsg());
        
        
        // ----- NOW WE CAN BEGIN THE UPLOAD -----
        logBeginExperimentUpload();
        long start = System.currentTimeMillis();
        
        try {
            this.uploadedExptId = exptUploader.uploadRawData();
        }
        catch (UploadException ex) {
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            return;
        }
        
        // ----- UPLOAD SEARCH DATA
        if(uploadSearch) {
            try {
                this.uploadedSearchId = exptUploader.uploadSearchData(this.uploadedExptId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        // ----- UPLOAD ANALYSIS DATA
        if(uploadAnalysis) {
            try {
                exptUploader.uploadAnalysisData(this.uploadedSearchId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        long end = System.currentTimeMillis();
        logEndExperimentUpload(exptUploader, start, end);
        
    }

    private MsExperimentUploader initializeExperimentUploader() throws UploadException  {
        
        MsExperimentUploader exptUploader = new MsExperimentUploader();
        exptUploader.setDirectory(rawDataDirectory);
        exptUploader.setRemoteDirectory(remoteRawDataDirectory);
        exptUploader.setRemoteServer(remoteServer);
        
        // Get the raw data uploader
        log.info("Initializing RawDataUploadService");
        RawDataUploadService rdus = getRawDataUploader(rawDataDirectory, remoteRawDataDirectory);
        exptUploader.setRawDataUploader(rdus);
        
        // We cannot upload analysis data without uploading search data first.
        if(uploadAnalysis && !uploadSearch) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Cannot upload analysis results without serach results");
            throw ex;
        }
        
        // Get the search data uploader
        if(uploadSearch) {
            log.info("Initializing SearchDataUploadService");
            SearchDataUploadService sdus = getSearchDataUploader(searchDirectory, 
                    remoteServer, remoteSearchDataDirectory, searchDate);
            exptUploader.setSearchDataUploader(sdus);
        }
        // Get the analysis data uploader
        if(uploadAnalysis) {
            log.info("Initializing AnalysisDataUploadService");
            AnalysisDataUploadService adus = getAnalysisDataUploader(analysisDirectory);
            exptUploader.setAnalysisDataUploader(adus);
        }
        
        return exptUploader;
    }

    private AnalysisDataUploadService getAnalysisDataUploader(String dataDirectory) throws UploadException {
        AnalysisDataUploadService adus = null;
        try {
            adus = UploadServiceFactory.instance().getAnalysisDataUploadService(dataDirectory);
            adus.setSearchProgram(Program.SEQUEST);
        }
        catch (UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting AnalysisDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return adus;
    }
    
    private SearchDataUploadService getSearchDataUploader(String dataDirectory,
            String remoteServer, String remoteDirectory, Date searchDate) throws UploadException {
        SearchDataUploadService sdus = null;
        try {
            sdus = UploadServiceFactory.instance().getSearchDataUploadService(dataDirectory);
            sdus.setRemoteServer(remoteServer);
            sdus.setRemoteDirectory(remoteDirectory);
            sdus.setSearchDate(searchDate);
            sdus.checkResultChargeMass(doScanChargeMassCheck);
        }
        catch (UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting SearchDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return sdus;
    }

    private RawDataUploadService getRawDataUploader(String dataDirectory, String remoteDirectory) throws UploadException {
        
        RawDataUploadService rdus = null;
        try {
            rdus = UploadServiceFactory.instance().getRawDataUploadService(dataDirectory);
            rdus.setRemoteDirectory(remoteDirectory);
        }
        catch(UploadServiceFactoryException e1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("Error getting RawDataUploadService: "+e1.getMessage());
            throw ex;
        }
        return rdus;
    }
    
    public void uploadData(int experimentId) {
        
        resetUploader();
        MsExperimentDAO exptDao = DAOFactory.instance().getMsExperimentDAO();
        MsExperiment expt = exptDao.loadExperiment(experimentId);
        if (expt == null) {
            UploadException ex = new UploadException(ERROR_CODE.EXPT_NOT_FOUND);
            ex.appendErrorMessage("Experiment ID: "+experimentId+" does not exist in the database.");
            ex.appendErrorMessage("!!!EXPERIMENT WILL NOT BE UPLOADED!!!");
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        this.remoteServer = expt.getServerAddress();
        this.uploadedExptId = experimentId;
        
        
        log.info("INITIALIZING EXPERIMENT UPLOAD"+
                "\n\tTime: "+(new Date().toString()));
        
        
        // ----- INITIALIZE THE EXPERIMENT UPLOADER
        MsExperimentUploader exptUploader = null;
        try {
            exptUploader = initializeExperimentUploader();
        }
        catch (UploadException e) {
            uploadExceptionList.add(e);
            log.error(e.getMessage(), e);
            return;
        }
        
        // ----- CHECKS BEFORE BEGINNING UPLOAD -----
        log.info("Starting pre-upload checks..");
        if(!exptUploader.preUploadCheckPassed()) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage(exptUploader.getPreUploadCheckMsg());
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            return;
        }
        log.info(exptUploader.getPreUploadCheckMsg());
        
        
        // ----- NOW WE CAN BEGIN THE UPLOAD -----
        logBeginExperimentUpload();
        long start = System.currentTimeMillis();
        
        // ----- UPDATE THE LAST UPDATE DATE FOR THE EXPERIMENT
        updateLastUpdateDate(experimentId);
        
        // ----- UPLOAD SCAN DATA
        try {
            exptUploader.uploadRawData(experimentId);
        }
        catch (UploadException ex) {
            uploadExceptionList.add(ex);
            log.error(ex.getMessage(), ex);
            log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            return;
        }
        
        // ----- UPLOAD SEARCH DATA
        if(uploadSearch) {
            // If the search is already uploaded, don't re-upload it.
            int searchId = 0;
            try {
                searchId = getExperimentSequestSearchId(this.uploadedExptId);
            }
            catch (Exception e) {
                UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
                ex.appendErrorMessage(e.getMessage());
                uploadExceptionList.add(ex);
                log.error(ex.getMessage());
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
            }
            if(searchId == 0) {
                try {
                    this.uploadedSearchId = exptUploader.uploadSearchData(this.uploadedExptId);
                }
                catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
                }
            }
            else {
                this.uploadedSearchId = searchId;
                log.info("Search was uploaded previously. SearchID: "+uploadedSearchId);
            }
        }
        
        // ----- UPLOAD ANALYSIS DATA
        if(uploadAnalysis) {
            try {
                exptUploader.uploadAnalysisData(this.uploadedSearchId);
            }
            catch (UploadException ex) {
                uploadExceptionList.add(ex);
                log.error(ex.getMessage(), ex);
                log.error("ABORTING EXPERIMENT UPLOAD!!!\n\tTime: "+(new Date()).toString()+"\n\n");
                return;
            }
        }
        
        long end = System.currentTimeMillis();
        logEndExperimentUpload(exptUploader, start, end);
       
    }

    private int getExperimentSequestSearchId(int uploadedExptId2) throws Exception {
       
        MsSearchDAO searchDao = DAOFactory.instance().getMsSearchDAO();
        List<Integer> searchIds = searchDao.getSearchIdsForExperiment(uploadedExptId2);
        
        if(searchIds.size() == 0)
            return 0;
        
        int sequestSearchId = 0;
        for(Integer id: searchIds) {
            MsSearch search = searchDao.loadSearch(id);
            if(search.getSearchProgram() == Program.SEQUEST) {
                if(sequestSearchId != 0)
                    throw new Exception("Multiple sequest search ids found for experimentID: "+uploadedExptId2);
                sequestSearchId = id;
            }
        }
        return sequestSearchId;
    }

    private void updateLastUpdateDate(int experimentId) {
        MsExperimentDAO experimentDao = DAOFactory.instance().getMsExperimentDAO();
        experimentDao.updateLastUpdateDate(experimentId);
        
    }

    private void deleteOldSearchesForExperiment(int experimentId) {
        MsSearchDAO searchDao = DAOFactory.instance().getMsSearchDAO();
        List<Integer> searchIds = searchDao.getSearchIdsForExperiment(experimentId);
        for (Integer id: searchIds) {
            if (id != null)
                searchDao.deleteSearch(id);
        }
    }

    
    private void logEndExperimentUpload(MsExperimentUploader uploader, long start, long end) {
        log.info("END EXPERIMENT UPLOAD: "+((end - start)/(1000L))+"seconds"+
                "\n\tTime: "+(new Date().toString())+"\n"+
                uploader.getUploadSummary());
    }

    private void logBeginExperimentUpload() {
        
        StringBuilder msg = new StringBuilder();
        msg.append("BEGIN EXPERIMENT UPLOAD");
        msg.append("\n\tRemote server: "+remoteServer);
        msg.append("\n\tRAW DATA ");
        msg.append("\n\t\t Directory: "+rawDataDirectory);
        msg.append("\n\t\t Remote Directory: "+remoteRawDataDirectory);
        if(uploadSearch) {
            msg.append("\n\tSEARCH DATA");
            msg.append("\n\t\t Directory: "+searchDirectory);
            msg.append("\n\t\t Remote Directory: "+remoteSearchDataDirectory);
        }
        if(uploadAnalysis) {
            msg.append("\n\tANALYSIS DATA");
            msg.append("\n\t\t Directory: "+analysisDirectory);
            msg.append("\n\tTime: "+(new Date().toString()));
        }
        
        log.info(msg.toString());
    }

   
   
    
//    // upload percolator sqt files
//    private int uploadPercolatorSearch(Set<String> filenames, Map<String, Integer> runIdMap, final Date searchDate) {
//        
//        // first upload the sqt files to populate the core search tables
//        SearchParamsDataProvider paramsProvider = new SequestParamsParser();
//        PeptideResultBuilder peptbuilder = SequestResultPeptideBuilder.instance();
//        
//        BaseSQTDataUploadService service = new BaseSQTDataUploadService(paramsProvider, peptbuilder, Program.PERCOLATOR);
//        if(isMacCossData) 
//            service.doScanChargeMassCheck(true);
//        
//        int searchId = service.uploadSearch(uploadedExptId, uploadDirectory, filenames, runIdMap, 
//                        remoteServer, remoteDirectory, new java.sql.Date(searchDate.getTime()));
//        
//        this.uploadExceptionList.addAll(service.getUploadExceptionList());
//        this.numSearchesToUpload = service.getNumSearchesToUpload();
//        this.numSearchesUploaded = service.getNumSearchesUploaded();
//        
//        // if the search information could not be uploaded don't go any further
//        if(uploadExceptionList.size() > 0) {
//            return searchId;
//        }
//        
//        // now upload the Percolator search results
//        PercolatorSQTDataUploadService percService = new PercolatorSQTDataUploadService();
//        MsRunSearchDAO runSearchDao = DAOFactory.instance().getMsRunSearchDAO();
//        List<Integer> runSearchIds = runSearchDao.loadRunSearchIdsForSearch(searchId);
//        Map<String, Integer> runSearchIdMap = new HashMap<String, Integer>(runSearchIds.size());
//        for(Integer id: runSearchIds) {
//            String filename = runSearchDao.loadFilenameForRunSearch(id);
//            runSearchIdMap.put(filename, id);
//        }
//        percService.uploadPostSearchAnalysis(searchId, Program.SEQUEST, uploadDirectory, filenames, 
//                runSearchIdMap, remoteDirectory);
//        
//        this.uploadExceptionList.addAll(percService.getUploadExceptionList());
//        
//        return searchId;
//    }
    
    
    public static int getScanIdFor(String runFileScanString, int searchId) {
        // parse the filename to get the filename, scan number and charge
        // e.g. NE063005ph8s02.17247.17247.2
        Matcher match = fileNamePattern.matcher(runFileScanString);
        if (!match.matches()) {
            log.error("!!!INVALID FILENAME FROM DTASELECT RESULT: "+runFileScanString);
            return 0;
        }
        String runFileName = match.group(1)+".ms2";
        int scanNum = Integer.parseInt(match.group(2));
        
        MsRunDAO runDao = DAOFactory.instance().getMsRunDAO();
        int runId = runDao.loadRunIdForSearchAndFileName(searchId, runFileName);
        if (runId == 0) {
            log.error("!!!NO RUN FOUND FOR SearchId: "+searchId+"; fileName: "+runFileName);
            return 0;
        }
        
        MsScanDAO scanDao = DAOFactory.instance().getMsScanDAO();
        int scanId = scanDao.loadScanIdForScanNumRun(scanNum, runId);
        if (scanId == 0) {
            log.error("!!!NO SCAN FOUND FOR SCAN NUMBER: "+scanNum+"; runId: "+runId+"; fileName: "+runFileName);
            return 0;
        }
        
        return scanId;
    }
    
    public static void main(String[] args) throws UploadException {
        long start = System.currentTimeMillis();

        
//        for(int i = 0; i < 10; i++) {
        String directory = args[0];
        
        if(directory == null || directory.length() == 0 || !(new File(directory).exists()))
            System.out.println("Invalid directory: "+directory);
        
        boolean maccossData = Boolean.parseBoolean(args[1]);
        
        System.out.println("Directory: "+directory+"; Maccoss Data: "+maccossData);
        
        MsDataUploader uploader = new MsDataUploader();
        uploader.setRemoteServer("local");
        uploader.setRawDataDirectory(directory);
        uploader.setSearchDirectory(directory);
        uploader.setRemoteRawDataDirectory(directory);
        uploader.setRemoteSearchDataDirectory(directory);
        uploader.setSearchDate(new Date());
        uploader.checkResultChargeMass(maccossData);
        
        uploader.uploadData();
//        }
        long end = System.currentTimeMillis();
        log.info("TOTAL TIME: "+((end - start)/(1000L))+"seconds.");
    }
}