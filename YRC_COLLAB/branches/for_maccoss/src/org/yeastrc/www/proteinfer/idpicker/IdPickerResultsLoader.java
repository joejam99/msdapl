package org.yeastrc.www.proteinfer.idpicker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.analysis.MsRunSearchAnalysisDAO;
import org.yeastrc.ms.dao.analysis.percolator.PercolatorResultDAO;
import org.yeastrc.ms.dao.run.MsScanDAO;
import org.yeastrc.ms.dao.search.MsRunSearchDAO;
import org.yeastrc.ms.dao.search.prolucid.ProlucidSearchResultDAO;
import org.yeastrc.ms.dao.search.sequest.SequestSearchResultDAO;
import org.yeastrc.ms.domain.search.MsSearchResult;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.nr_seq.NRProtein;
import org.yeastrc.nr_seq.NRProteinFactory;
import org.yeastrc.www.compare.CommonNameLookupUtil;
import org.yeastrc.www.compare.FastaProteinLookupUtil;
import org.yeastrc.www.compare.ProteinDatabaseLookupUtil;
import org.yeastrc.www.compare.ProteinListing;

import edu.uwpr.protinfer.PeptideDefinition;
import edu.uwpr.protinfer.ProteinInferenceProgram;
import edu.uwpr.protinfer.database.dao.ProteinferDAOFactory;
import edu.uwpr.protinfer.database.dao.ibatis.ProteinferProteinDAO;
import edu.uwpr.protinfer.database.dao.ibatis.ProteinferSpectrumMatchDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerInputDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerPeptideBaseDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerPeptideDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerProteinBaseDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerRunDAO;
import edu.uwpr.protinfer.database.dao.idpicker.ibatis.IdPickerSpectrumMatchDAO;
import edu.uwpr.protinfer.database.dto.GenericProteinferIon;
import edu.uwpr.protinfer.database.dto.ProteinFilterCriteria;
import edu.uwpr.protinfer.database.dto.ProteinferIon;
import edu.uwpr.protinfer.database.dto.ProteinferProtein;
import edu.uwpr.protinfer.database.dto.ProteinferSpectrumMatch;
import edu.uwpr.protinfer.database.dto.ProteinFilterCriteria.SORT_BY;
import edu.uwpr.protinfer.database.dto.ProteinferInput.InputType;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerInput;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerIon;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerPeptide;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerPeptideBase;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerProteinBase;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerRun;
import edu.uwpr.protinfer.util.TimeUtils;

public class IdPickerResultsLoader {

    private static final ProteinferDAOFactory pinferDaoFactory = ProteinferDAOFactory.instance();
    private static final org.yeastrc.ms.dao.DAOFactory msDataDaoFactory = org.yeastrc.ms.dao.DAOFactory.instance();
    private static final MsScanDAO scanDao = msDataDaoFactory.getMsScanDAO();
    private static final MsRunSearchDAO rsDao = msDataDaoFactory.getMsRunSearchDAO();
    private static final MsRunSearchAnalysisDAO rsaDao = msDataDaoFactory.getMsRunSearchAnalysisDAO();
    
    private static final SequestSearchResultDAO seqResDao = msDataDaoFactory.getSequestResultDAO();
    private static final ProlucidSearchResultDAO plcResDao = msDataDaoFactory.getProlucidResultDAO();
    private static final PercolatorResultDAO percResDao = msDataDaoFactory.getPercolatorResultDAO();
    
    private static final ProteinferSpectrumMatchDAO psmDao = pinferDaoFactory.getProteinferSpectrumMatchDao();
    private static final IdPickerSpectrumMatchDAO idpPsmDao = pinferDaoFactory.getIdPickerSpectrumMatchDao();
    private static final IdPickerPeptideDAO idpPeptDao = pinferDaoFactory.getIdPickerPeptideDao();
    private static final IdPickerPeptideBaseDAO idpPeptBaseDao = pinferDaoFactory.getIdPickerPeptideBaseDao();
    private static final IdPickerProteinBaseDAO idpProtBaseDao = pinferDaoFactory.getIdPickerProteinBaseDao();
    private static final ProteinferProteinDAO protDao = pinferDaoFactory.getProteinferProteinDao();
    private static final IdPickerInputDAO inputDao = pinferDaoFactory.getIdPickerInputDao();
    private static final IdPickerRunDAO idpRunDao = pinferDaoFactory.getIdPickerRunDao();
    
//    private static final ProteinPropertiesDAO proteinPropsDao = pinferDaoFactory.getProteinPropertiesDao();
    
    private static final Logger log = Logger.getLogger(IdPickerResultsLoader.class);
    
    private IdPickerResultsLoader(){}
    
    //---------------------------------------------------------------------------------------------------
    // Get a list of protein IDs with the given filtering criteria
    //---------------------------------------------------------------------------------------------------
    public static List<Integer> getProteinIds(int pinferId, ProteinFilterCriteria filterCriteria) {
        long s = System.currentTimeMillis();
        List<Integer> proteinIds = idpProtBaseDao.getFilteredSortedProteinIds(pinferId, filterCriteria);
        
        // filter by accession, if required
        if(filterCriteria.getAccessionLike() != null) {
            proteinIds = IdPickerResultsLoader.filterByProteinAccession(pinferId,
                    proteinIds, null, 
                    filterCriteria.getAccessionLike());
        }
        // filter by description, if required
        if(filterCriteria.getDescriptionLike() != null) {
            proteinIds = IdPickerResultsLoader.filterByProteinDescription(pinferId, proteinIds, filterCriteria.getDescriptionLike(), true);
        }
        if(filterCriteria.getDescriptionNotLike() != null) {
            proteinIds = IdPickerResultsLoader.filterByProteinDescription(pinferId, proteinIds, filterCriteria.getDescriptionNotLike(), false);
        }
        // filter by molecular wt, if required
        if(filterCriteria.hasMolecularWtFilter()) {
            proteinIds = ProteinPropertiesFilter.filterByMolecularWt(pinferId, proteinIds,
                    filterCriteria.getMinMolecularWt(), filterCriteria.getMaxMolecularWt());
            if(filterCriteria.getSortBy() == SORT_BY.MOL_WT)
                proteinIds = ProteinPropertiesSorter.sortIdsByMolecularWt(proteinIds, pinferId, filterCriteria.isGroupProteins());
        }
        // filter by pI, if required
        if(filterCriteria.hasPiFilter()) {
            proteinIds = ProteinPropertiesFilter.filterByPi(pinferId, proteinIds,
                    filterCriteria.getMinPi(), filterCriteria.getMaxPi());
            if(filterCriteria.getSortBy() == SORT_BY.PI)
                proteinIds = ProteinPropertiesSorter.sortIdsByPi(proteinIds, pinferId, filterCriteria.isGroupProteins());
                    
        }
        
        log.info("Returned "+proteinIds.size()+" protein IDs for protein inference ID: "+pinferId);
        long e = System.currentTimeMillis();
        log.info("Time: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        return proteinIds;
    }
    
    public static List<Integer> filterByProteinAccession(int pinferId,
            List<Integer> allProteinIds,
            Map<Integer, String> proteinAccessionMap, String accessionLike) {
        
        // First get the NRSEQ protein IDs that match the accession(s) 
        Set<String> reqAcc = new HashSet<String>();
        String[] tokens = accessionLike.split(",");
        for(String tok: tokens)
            // In MySQL string comparisons are NOT case sensitive unless one of the operands is a binary string
            // We are converting to lower case in case we will look up in a proteinAccessionMap.
            reqAcc.add(tok.trim().toLowerCase()); 
        
        List<Integer> filtered = new ArrayList<Integer>();
        
        
        // If we have a accession map look in there
        if(proteinAccessionMap != null) {
        
            for(int id: allProteinIds) {
                String acc = proteinAccessionMap.get(id);
                if(acc != null) acc = acc.toLowerCase();
                // first check if the exact accession is given to us
                if(reqAcc.contains(acc)) {
                    filtered.add(id);
                    continue;
                }
                // we may have a partial accession
                for(String ra: reqAcc) {
                    if(acc.contains(ra)) {
                        filtered.add(id);
                        break;
                    }
                }
            }
        }
        
        // Look in the database for matching ids.
        else {
            List<Integer> found = FastaProteinLookupUtil.getInstance().getProteinIdsForNames(new ArrayList<String>(reqAcc), pinferId);
            
            // get the corresponding protein inference protein ids
            if(found.size() > 0) {
                List<Integer> piProteinIds = protDao.getProteinIdsForNrseqIds(pinferId, new ArrayList<Integer>(found));
                Collections.sort(piProteinIds);
                for(int id: allProteinIds) {
                    if(Collections.binarySearch(piProteinIds, id) >= 0)
                        filtered.add(id);
                }
            }
        }
        
        return filtered;
    }
    
    public static List<Integer> filterByProteinDescription(int pinferId,
            List<Integer> storedProteinIds, String descriptionLike, boolean include) {
        
        List<Integer> filtered = new ArrayList<Integer>();
        
        Set<String> reqDescriptions = new HashSet<String>();
        String[] tokens = descriptionLike.split(",");
        for(String tok: tokens)
            // In MySQL string comparisons are NOT case sensitive unless one of the operands is a binary string
            reqDescriptions.add(tok.trim()); 
        
        // First get the NRSEQ protein IDs that match the description terms
        List<Integer> found = FastaProteinLookupUtil.getInstance().getProteinIdsForDescriptions(new ArrayList<String>(reqDescriptions), pinferId);
        
        if(found.size() > 0) {
            // Get the protein inference IDs corresponding to the matching NRSEQ IDs.
            List<Integer> piProteinIds = protDao.getProteinIdsForNrseqIds(pinferId, new ArrayList<Integer>(found));
            Collections.sort(piProteinIds);
            for(int id: storedProteinIds) {
                int contains = Collections.binarySearch(piProteinIds, id);
                if(include && contains >= 0)
                    filtered.add(id);
                else if(!include && contains < 0)
                    filtered.add(id);
            }
        }
        
        return filtered;
        
    }
    
    //---------------------------------------------------------------------------------------------------
    // Get a list of protein IDs with the given sorting criteria
    //---------------------------------------------------------------------------------------------------
    public static List<Integer> getSortedProteinIds(int pinferId, PeptideDefinition peptideDef, 
            List<Integer> proteinIds, SORT_BY sortBy, boolean groupProteins) {
        
        long s = System.currentTimeMillis();
        List<Integer> allIds = null;
        if(sortBy == SORT_BY.CLUSTER_ID) {
            allIds = idpProtBaseDao.sortProteinIdsByCluster(pinferId);
        }
        else if (sortBy == SORT_BY.GROUP_ID) {
            allIds = idpProtBaseDao.sortProteinIdsByGroup(pinferId);
        }
        else if (sortBy == SORT_BY.COVERAGE) {
            allIds = idpProtBaseDao.sortProteinIdsByCoverage(pinferId, groupProteins);
        }
        else if(sortBy == SORT_BY.NSAF) {
            allIds = idpProtBaseDao.sortProteinsByNSAF(pinferId, groupProteins);
        }
        else if(sortBy == SORT_BY.VALIDATION_STATUS) {
            allIds = idpProtBaseDao.sortProteinIdsByValidationStatus(pinferId);
        }
        else if (sortBy == SORT_BY.NUM_PEPT) {
            allIds = idpProtBaseDao.sortProteinIdsByPeptideCount(pinferId, peptideDef, groupProteins);
        }
        else if (sortBy == SORT_BY.NUM_UNIQ_PEPT) {
            allIds = idpProtBaseDao.sortProteinIdsByUniquePeptideCount(pinferId, peptideDef, groupProteins);
        }
        else if (sortBy == SORT_BY.NUM_SPECTRA) {
            allIds = idpProtBaseDao.sortProteinIdsBySpectrumCount(pinferId, groupProteins);
        }
        else if (sortBy == SORT_BY.MOL_WT) {
            List<Integer> sortedIds = ProteinPropertiesSorter.sortIdsByMolecularWt(proteinIds, pinferId, groupProteins);
            return sortedIds;
        }
        else if(sortBy == SORT_BY.PI) {
            List<Integer> sortedIds = ProteinPropertiesSorter.sortIdsByPi(proteinIds, pinferId, groupProteins);
            return sortedIds;
        }
//        else if (sortBy == SORT_BY.ACCESSION) {
//            allIds = sortIdsByAccession(proteinIds);
//        }
        if(allIds == null) {
            log.warn("Could not get sorted order for all protein IDs for protein inference run: "+pinferId);
        }
        
        // we want the sorted order from allIds but only want to keep the ids in the current 
        // filtered list.
        // remove the ones from allIds that are not in the current filtered list. 
        Set<Integer> currentOrder = new HashSet<Integer>((int) (proteinIds.size() * 1.5));
        currentOrder.addAll(proteinIds);
        Iterator<Integer> iter = allIds.iterator();
        while(iter.hasNext()) {
            Integer protId = iter.next();
            if(!currentOrder.contains(protId))
                iter.remove();
        }
        
        long e = System.currentTimeMillis();
        log.info("Time for resorting filtered IDs: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        return allIds;
    }
    
    public static Map<Integer, String> getProteinAccessionMap(int pinferId) {
        log.info("Building Protein Accession map");
        long s = System.currentTimeMillis();
        List<ProteinferProtein> proteins = protDao.loadProteins(pinferId);
        Map<Integer, String> map = new HashMap<Integer, String>((int) (proteins.size() * 1.5));
        long e = System.currentTimeMillis();
        log.info("Time to get all proteins: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        s = System.currentTimeMillis();
        
        FastaProteinLookupUtil fplUtil = FastaProteinLookupUtil.getInstance();
        List<Integer> dbIds = ProteinDatabaseLookupUtil.getInstance().getDatabaseIdsForProteinInference(pinferId);
        
        for(ProteinferProtein protein: proteins) {
            String accession = fplUtil.getProteinListing(protein.getNrseqProteinId(), dbIds).getAllNames();
            map.put(protein.getId(), accession);
        }
        e = System.currentTimeMillis();
        log.info("Time to assign protein accessions: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        return map;
    }
    
    public static List<Integer> sortIdsByAccession(List<Integer> proteinIds, Map<Integer, String> proteinAccessionMap) {
        
        List<ProteinIdAccession> accMap = new ArrayList<ProteinIdAccession>(proteinIds.size());
        
        for(int id: proteinIds) {
            accMap.add(new ProteinIdAccession(id, proteinAccessionMap.get(id)));
        }
        Collections.sort(accMap, new Comparator<ProteinIdAccession>() {
            public int compare(ProteinIdAccession o1, ProteinIdAccession o2) {
                return o1.accession.toLowerCase().compareTo(o2.accession.toLowerCase());
            }});
        List<Integer> sortedIds = new ArrayList<Integer>(proteinIds.size());
        for(ProteinIdAccession pa: accMap) {
            sortedIds.add(pa.proteinId);
        }
        return sortedIds;
    }
    
    
    
    private static class ProteinIdAccession {
        int proteinId;
        String accession;
        public ProteinIdAccession(int proteinId, String accession) {
            this.proteinId = proteinId;
            this.accession = accession;
        }
    }

    //---------------------------------------------------------------------------------------------------
    // Get a list of proteins
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerProtein> getProteins(int pinferId, List<Integer> proteinIds, 
            PeptideDefinition peptideDef) {
        long s = System.currentTimeMillis();
        List<WIdPickerProtein> proteins = new ArrayList<WIdPickerProtein>(proteinIds.size());
        List<Integer> fastaDatabaseIds = ProteinDatabaseLookupUtil.getInstance().getDatabaseIdsForProteinInference(pinferId);
        for(int id: proteinIds) 
            proteins.add(getIdPickerProtein(id, peptideDef, fastaDatabaseIds));
        long e = System.currentTimeMillis();
        log.info("Time to get WIdPickerProteins: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        return proteins;
    }
    
    public static WIdPickerProtein getIdPickerProtein(int pinferProteinId, 
            PeptideDefinition peptideDef, List<Integer> databaseIds) {
        IdPickerProteinBase protein = idpProtBaseDao.loadProtein(pinferProteinId);
        protein.setPeptideDefinition(peptideDef);
        return getWIdPickerProtein(protein, databaseIds);
    }
    
    public static WIdPickerProtein getIdPickerProtein(int pinferId, int pinferProteinId, 
            PeptideDefinition peptideDef) {
        List<Integer> fastaDatabaseIds = ProteinDatabaseLookupUtil.getInstance().getDatabaseIdsForProteinInference(pinferId);
       return getIdPickerProtein(pinferProteinId, peptideDef, fastaDatabaseIds);
    }
    
    private static WIdPickerProtein getWIdPickerProtein(IdPickerProteinBase protein, List<Integer> databaseIds) {
        WIdPickerProtein wProt = new WIdPickerProtein(protein);
        // set the accession and description for the proteins.  
        // This requires querying the NRSEQ database
        assignProteinAccessionDescription(wProt, databaseIds);
        
        // get the molecular weight for the protein
        assignProteinProperties(wProt);
        return wProt;
    }
    
    //---------------------------------------------------------------------------------------------------
    // Get all the proteins in a group
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerProtein> getGroupProteins(int pinferId, int groupId, 
            PeptideDefinition peptideDef) {
        
        long s = System.currentTimeMillis();
        
        List<IdPickerProteinBase> groupProteins = idpProtBaseDao.loadIdPickerGroupProteins(pinferId, groupId);
        
        List<WIdPickerProtein> proteins = new ArrayList<WIdPickerProtein>(groupProteins.size());
        List<Integer> fastaDatabaseIds = ProteinDatabaseLookupUtil.getInstance().getDatabaseIdsForProteinInference(pinferId);
        
        for(IdPickerProteinBase prot: groupProteins) {
            prot.setPeptideDefinition(peptideDef);
            proteins.add(getWIdPickerProtein(prot, fastaDatabaseIds));
        }
        
        long e = System.currentTimeMillis();
        log.info("Time to get proteins in a group: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        return proteins;
    }
    
    //---------------------------------------------------------------------------------------------------
    // Get a list of protein groups
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerProteinGroup> getProteinGroups(int pinferId, List<Integer> proteinIds, List<Integer> allProteinIds, 
            PeptideDefinition peptideDef) {
        return getProteinGroups(pinferId, proteinIds, allProteinIds, true, peptideDef);
    }
    
    public static List<WIdPickerProteinGroup> getProteinGroups(int pinferId, List<Integer> proteinIds, List<Integer> allProteinIds, boolean append, 
            PeptideDefinition peptideDef) {
        long s = System.currentTimeMillis();
        List<WIdPickerProtein> proteins = getProteins(pinferId, proteinIds, peptideDef);
        
        if(proteins.size() == 0) {
            return new ArrayList<WIdPickerProteinGroup>(0);
        }
        
        List<Integer> fastaDatabaseIds = ProteinDatabaseLookupUtil.getInstance().getDatabaseIdsForProteinInference(pinferId);
        
        if(append) {
            // protein Ids should be sorted by groupID. If the proteins at the top of the list
            // does not have all members of the group in the list, add them
            int groupId_top = proteins.get(0).getProtein().getGroupId();
            List<IdPickerProteinBase> groupProteins = idpProtBaseDao.loadIdPickerGroupProteins(pinferId, groupId_top);
            for(IdPickerProteinBase prot: groupProteins) {
                if(!proteinIds.contains(prot.getId()) && allProteinIds.contains(prot.getId())) {
                    prot.setPeptideDefinition(peptideDef);
                    proteins.add(0, getWIdPickerProtein(prot, fastaDatabaseIds));
                }
            }

            // protein Ids should be sorted by groupID. If the proteins at the bottom of the list
            // does not have all members of the group in the list, add them
            int groupId_last = proteins.get(proteins.size() - 1).getProtein().getGroupId();
            if(groupId_last != groupId_top) {
                groupProteins = idpProtBaseDao.loadIdPickerGroupProteins(pinferId, groupId_last);
                for(IdPickerProteinBase prot: groupProteins) {
                    if(!proteinIds.contains(prot.getId()) && allProteinIds.contains(prot.getId())) {
                        prot.setPeptideDefinition(peptideDef);
                        proteins.add(getWIdPickerProtein(prot, fastaDatabaseIds));
                    }
                }
            }
        }
       
        if(proteins.size() == 0)
            return new ArrayList<WIdPickerProteinGroup>(0);
        
        int currGrpId = -1;
        List<WIdPickerProtein> grpProteins = null;
        List<WIdPickerProteinGroup> groups = new ArrayList<WIdPickerProteinGroup>();
        for(WIdPickerProtein prot: proteins) {
            if(prot.getProtein().getGroupId() != currGrpId) {
                if(grpProteins != null && grpProteins.size() > 0) {
                    WIdPickerProteinGroup grp = new WIdPickerProteinGroup(grpProteins);
                    groups.add(grp);
                }
                currGrpId = prot.getProtein().getGroupId();
                grpProteins = new ArrayList<WIdPickerProtein>();
            }
            grpProteins.add(prot);
        }
        if(grpProteins != null && grpProteins.size() > 0) {
            WIdPickerProteinGroup grp = new WIdPickerProteinGroup(grpProteins);
            groups.add(grp);
        }
        
        long e = System.currentTimeMillis();
        log.info("Time to get WIdPickerProteinsGroups: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        return groups;
    }
    
    //---------------------------------------------------------------------------------------------------
    // NR_SEQ lookup 
    //---------------------------------------------------------------------------------------------------
    private static void assignProteinAccessionDescription(WIdPickerProtein wProt, List<Integer> databaseIds) {
        
        String[] acc_descr = getProteinAccessionDescription(wProt.getProtein().getNrseqProteinId(), databaseIds);
        wProt.setAccession(acc_descr[0]);
        wProt.setDescription(acc_descr[1]);
        wProt.setCommonName(acc_descr[2]);
    }
    
    private static String[] getProteinAccessionDescription(int nrseqProteinId, List<Integer> databaseIds) {
        return getProteinAccessionDescription(nrseqProteinId, databaseIds, true);
    }

    private static String[] getProteinAccessionDescription(int nrseqProteinId, List<Integer> databaseIds,
            boolean getCommonName) {
        
        ProteinListing listing = FastaProteinLookupUtil.getInstance().getProteinListing(nrseqProteinId, databaseIds);
        String accession = listing.getAllNames();
        String description = listing.getAllDescriptions();
        
        String commonName = "";
        if(getCommonName) {

            try {
                commonName = CommonNameLookupUtil.getInstance().getProteinListing(nrseqProteinId).getName();
            }
            catch (Exception e) {
                log.error("Exception getting common name for protein Id: "+nrseqProteinId, e);
            }
        }
        return new String[] {accession, description, commonName};
        
    }
    
    private static void assignProteinProperties(WIdPickerProtein wProt) {
        
        ProteinProperties props = ProteinPropertiesStore.getInstance().getProteinProperties(wProt.getProtein().getProteinferId(), wProt.getProtein());
        if(props != null) {
            wProt.setMolecularWeight( (float) (Math.round(props.getMolecularWt()*100) / 100.0));
            wProt.setPi( (float) (Math.round(props.getPi()*100) / 100.0));
        }
    }
    
    //---------------------------------------------------------------------------------------------------
    // IDPicker input summary
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerInputSummary> getIDPickerInputSummary(int pinferId) {
        
        List<IdPickerInput> inputSummary = inputDao.loadProteinferInputList(pinferId);
        List<WIdPickerInputSummary> wInputList = new ArrayList<WIdPickerInputSummary>(inputSummary.size());
        
        for(IdPickerInput input: inputSummary) {
            String filename = "";
            if(input.getInputType() == InputType.SEARCH)
                filename = rsDao.loadFilenameForRunSearch(input.getInputId());
            else if(input.getInputType() == InputType.ANALYSIS)
                filename = rsaDao.loadFilenameForRunSearchAnalysis(input.getInputId());
            else
                log.error("Unknown input type: "+input.getInputType().name());
            
            WIdPickerInputSummary winput = new WIdPickerInputSummary(input);
            winput.setFileName(filename);
            wInputList.add(winput);
        }
        Collections.sort(wInputList, new Comparator<WIdPickerInputSummary>() {
            @Override
            public int compare(WIdPickerInputSummary o1,
                    WIdPickerInputSummary o2) {
                return o1.getFileName().compareTo(o2.getFileName());
            }});
        return wInputList;
    }
    
    public static int getUniquePeptideCount(int pinferId) {
        return idpPeptBaseDao.getUniquePeptideSequenceCountForRun(pinferId);
    }
    //---------------------------------------------------------------------------------------------------
    // IDPicker result summary
    //---------------------------------------------------------------------------------------------------
    public static WIdPickerResultSummary getIdPickerResultSummary(int pinferId, List<Integer> proteinIds) {
        
        long s = System.currentTimeMillis();
        
        WIdPickerResultSummary summary = new WIdPickerResultSummary();
//        IdPickerRun run = idpRunDao.loadProteinferRun(pinferId);
//        summary.setUnfilteredProteinCount(run.getNumUnfilteredProteins());
        summary.setFilteredProteinCount(proteinIds.size());
        // parsimonious protein IDs
        List<Integer> parsimProteinIds = idpProtBaseDao.getIdPickerProteinIds(pinferId, true);
        Map<Integer, Integer> protGroupMap = idpProtBaseDao.getProteinGroupIds(pinferId, false);
        
        
        Set<Integer> groupIds = new HashSet<Integer>((int) (proteinIds.size() * 1.5));
        for(int id: proteinIds) {
            groupIds.add(protGroupMap.get(id));
        }
        summary.setFilteredProteinGroupCount(groupIds.size());
        
        groupIds.clear();
        int parsimCount = 0;
        Set<Integer> myIds = new HashSet<Integer>((int) (proteinIds.size() * 1.5));
        myIds.addAll(proteinIds);
        for(int id: parsimProteinIds) {
            if(myIds.contains(id))  {
                parsimCount++;
                groupIds.add(protGroupMap.get(id));
            }
        }
        summary.setFilteredParsimoniousProteinCount(parsimCount);
        summary.setFilteredParsimoniousProteinGroupCount(groupIds.size());
        
        long e = System.currentTimeMillis();
        log.info("Time to get WIdPickerResultSummary: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        return summary;
    }

    //---------------------------------------------------------------------------------------------------
    // Cluster Ids in the given protein inference run
    //---------------------------------------------------------------------------------------------------
    public static List<Integer> getClusterIds(int pinferId) {
        return idpProtBaseDao.getClusterIds(pinferId);
    }

    
    //---------------------------------------------------------------------------------------------------
    // Peptide ions for a protein group (sorted by sequence, modification state and charge
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerIon> getPeptideIonsForProteinGroup(int pinferId, int pinferProteinGroupId) {
        
        long s = System.currentTimeMillis();
        
        List<WIdPickerIon> ionList = new ArrayList<WIdPickerIon>();
        
        // get the id of one of the proteins in the group. All proteins in a group match the same peptides
        int proteinId = idpProtBaseDao.getIdPickerGroupProteinIds(pinferId, pinferProteinGroupId).get(0);
        
        IdPickerRun run = idpRunDao.loadProteinferRun(pinferId);
        
        // Get the program used to generate the input for protein inference
        Program inputGenerator = run.getInputGenerator();
        
        // Get the protein inference program used
        ProteinInferenceProgram pinferProgram = run.getProgram();
        
        
        if(pinferProgram == ProteinInferenceProgram.PROTINFER_SEQ ||
           pinferProgram == ProteinInferenceProgram.PROTINFER_PLCID) {
            List<IdPickerPeptide> peptides = idpPeptDao.loadPeptidesForProteinferProtein(proteinId);
            for(IdPickerPeptide peptide: peptides) {
                List<IdPickerIon> ions = peptide.getIonList();
                sortIonList(ions);
                for(IdPickerIon ion: ions) {
                    WIdPickerIon wIon = makeWIdPickerIon(ion, inputGenerator);
                    wIon.setIsUniqueToProteinGroup(peptide.isUniqueToProtein());
                    ionList.add(wIon);
                }
            }
        }
        else if (pinferProgram == ProteinInferenceProgram.PROTINFER_PERC ||
                pinferProgram == ProteinInferenceProgram.PROTINFER_PERC_OLD) {
            List<IdPickerPeptideBase> peptides = idpPeptBaseDao.loadPeptidesForProteinferProtein(proteinId);
            for(IdPickerPeptideBase peptide: peptides) {
                List<ProteinferIon> ions = peptide.getIonList();
                sortIonList(ions);
                for(ProteinferIon ion: ions) {
                    WIdPickerIon wIon = makeWIdPickerIon(ion, inputGenerator);
                    wIon.setIsUniqueToProteinGroup(peptide.isUniqueToProtein());
                    ionList.add(wIon);
                }
            }
        }
        else {
            log.error("Unknow version of IDPicker: "+pinferProgram);
        }
        
        long e = System.currentTimeMillis();
        log.info("Time to get peptide ions for pinferID: "+pinferId+
                ", proteinGroupID: "+pinferProteinGroupId+
                " -- "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        return ionList;
    }

    private static <I extends GenericProteinferIon<? extends ProteinferSpectrumMatch>>
            WIdPickerIon makeWIdPickerIon(I ion, Program inputGenerator) {
        ProteinferSpectrumMatch psm = ion.getBestSpectrumMatch();
        MsSearchResult origResult = getOriginalResult(psm.getMsRunSearchResultId(), inputGenerator);
        return new WIdPickerIon(ion, origResult);
    }

    private static void sortIonList(List<? extends GenericProteinferIon<?>> ions) {
        Collections.sort(ions, new Comparator<GenericProteinferIon<?>>() {
            public int compare(GenericProteinferIon<?> o1, GenericProteinferIon<?> o2) {
                if(o1.getModificationStateId() < o2.getModificationStateId())   return -1;
                if(o1.getModificationStateId() > o2.getModificationStateId())   return 1;
                if(o1.getCharge() < o2.getCharge())                             return -1;
                if(o2.getCharge() > o2.getCharge())                             return 1;
                return 0;
            }});
    }
    
    private static MsSearchResult getOriginalResult(int msRunSearchResultId, Program inputGenerator) {
        if(inputGenerator == Program.SEQUEST) {//|| inputGenerator == Program.EE_NORM_SEQUEST) {
            return seqResDao.load(msRunSearchResultId);
        }
        else if (inputGenerator == Program.PROLUCID) {
            return plcResDao.load(msRunSearchResultId);
        }
        else if (inputGenerator == Program.PERCOLATOR) {
            return percResDao.load(msRunSearchResultId);
        }
        else {
            log.warn("Unrecognized input generator for protein inference: "+inputGenerator);
            return null;
        }
    }
    
    
    //---------------------------------------------------------------------------------------------------
    // Peptide ions for a protein group (sorted by sequence, modification state and charge
    //---------------------------------------------------------------------------------------------------
    public static List<WIdPickerIonForProtein> getPeptideIonsForProtein(int pinferId, int proteinId) {
        
        long s = System.currentTimeMillis();
        
        List<WIdPickerIonForProtein> ionList = new ArrayList<WIdPickerIonForProtein>();
        
        IdPickerRun run = idpRunDao.loadProteinferRun(pinferId);
        
        // Get the program used to generate the input for protein inference
        Program inputGenerator = run.getInputGenerator();
        
        // Get the protein inference program used
        ProteinInferenceProgram pinferProgram = run.getProgram();
        
        ProteinferProtein protein = protDao.loadProtein(proteinId);
        String proteinSeq = getProteinSequence(protein);
        
        if(pinferProgram == ProteinInferenceProgram.PROTINFER_SEQ ||
           pinferProgram == ProteinInferenceProgram.PROTINFER_PLCID) {
            List<IdPickerPeptide> peptides = idpPeptDao.loadPeptidesForProteinferProtein(proteinId);
            for(IdPickerPeptide peptide: peptides) {
                List<Character>[] termResidues = getTerminalresidues(proteinSeq, peptide.getSequence());
                
                List<IdPickerIon> ions = peptide.getIonList();
                sortIonList(ions);
                for(IdPickerIon ion: ions) {
                    WIdPickerIonForProtein wIon = makeWIdPickerIonForProtein(ion, inputGenerator);
                    for(int i = 0; i < termResidues[0].size(); i++) {
                        wIon.addTerminalResidues(termResidues[0].get(i), termResidues[1].get(i));
                    }
                    wIon.setIsUniqueToProteinGroup(peptide.isUniqueToProtein());
                    ionList.add(wIon);
                }
            }
        }
        else if (pinferProgram == ProteinInferenceProgram.PROTINFER_PERC ||
                 pinferProgram == ProteinInferenceProgram.PROTINFER_PERC_OLD) {
            List<IdPickerPeptideBase> peptides = idpPeptBaseDao.loadPeptidesForProteinferProtein(proteinId);
            for(IdPickerPeptideBase peptide: peptides) {
                List<Character>[] termResidues = getTerminalresidues(proteinSeq, peptide.getSequence());
                
                List<ProteinferIon> ions = peptide.getIonList();
                sortIonList(ions);
                for(ProteinferIon ion: ions) {
                    WIdPickerIonForProtein wIon = makeWIdPickerIonForProtein(ion, inputGenerator);
                    for(int i = 0; i < termResidues[0].size(); i++) {
                        wIon.addTerminalResidues(termResidues[0].get(i), termResidues[1].get(i));
                    }
                    wIon.setIsUniqueToProteinGroup(peptide.isUniqueToProtein());
                    ionList.add(wIon);
                }
            }
        }
        else {
            log.error("Unknow version of IDPicker: "+pinferProgram);
        }
        
        long e = System.currentTimeMillis();
        log.info("Time to get peptide ions (with ALL spectra) for pinferID: "+pinferId+
                " -- "+TimeUtils.timeElapsedSeconds(s, e)+" seconds");
        
        return ionList;
    }
    
    private static List<Character>[] getTerminalresidues(String proteinSeq,
            String sequence) {
        List<Character> nterm = new ArrayList<Character>(2);
        List<Character> cterm = new ArrayList<Character>(2);
        int idx = proteinSeq.indexOf(sequence);
        while(idx != -1) {
            if(idx == 0)    nterm.add('-');
            else            nterm.add(proteinSeq.charAt(idx-1));
            if(idx+sequence.length() >= proteinSeq.length())
                cterm.add('-');
            else            cterm.add(proteinSeq.charAt(idx+sequence.length()));
            
            idx = proteinSeq.indexOf(sequence, idx+sequence.length());
        }
        return new List[]{nterm, cterm};
    }

    private static String getProteinSequence(ProteinferProtein protein) {
        NRProtein nrprot = null;
        NRProteinFactory nrpf = NRProteinFactory.getInstance();
        try {
            nrprot = (NRProtein)(nrpf.getProtein(protein.getNrseqProteinId()));
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }        

        return nrprot.getPeptide().getSequenceString();
    }
    
    private static <I extends GenericProteinferIon<? extends ProteinferSpectrumMatch>>
            WIdPickerIonForProtein makeWIdPickerIonForProtein(I ion, Program inputGenerator) {
        ProteinferSpectrumMatch psm = ion.getBestSpectrumMatch();
        MsSearchResult origResult = getOriginalResult(psm.getMsRunSearchResultId(), inputGenerator);
        return new WIdPickerIonForProtein(ion, origResult);
    }

    public static List<WIdPickerSpectrumMatch> getHitsForIon(int pinferIonId, Program inputGenerator, ProteinInferenceProgram pinferProgram) {
        
        List<? extends ProteinferSpectrumMatch> psmList = null;
        if(pinferProgram == ProteinInferenceProgram.PROTINFER_SEQ ||
           pinferProgram == ProteinInferenceProgram.PROTINFER_PLCID) {
            psmList = idpPsmDao.loadSpectrumMatchesForIon(pinferIonId);
        }
        else if (pinferProgram == ProteinInferenceProgram.PROTINFER_PERC ||
                 pinferProgram == ProteinInferenceProgram.PROTINFER_PERC_OLD) {
            psmList = psmDao.loadSpectrumMatchesForIon(pinferIonId);
        }
        else {
            log.error("Unknow version of IDPicker: "+pinferProgram);
        }
        
        List<WIdPickerSpectrumMatch> wPsmList = new ArrayList<WIdPickerSpectrumMatch>(psmList.size());
        for(ProteinferSpectrumMatch psm: psmList) {
            MsSearchResult origResult = getOriginalResult(psm.getMsRunSearchResultId(), inputGenerator);
            WIdPickerSpectrumMatch wPsm = new WIdPickerSpectrumMatch(psm, origResult);
            int scanNum = scanDao.load(origResult.getScanId()).getStartScanNum();
            wPsm.setScanNumber(scanNum);
            wPsmList.add(wPsm);
        }
        
        return wPsmList;
    }
    

    
    //---------------------------------------------------------------------------------------------------
    // Protein and Peptide groups for a cluster
    //--------------------------------------------------------------------------------------------------- 
    public static WIdPickerCluster getIdPickerCluster(int pinferId, int clusterId, 
            PeptideDefinition peptideDef) {
       
        List<Integer> protGroupIds = idpProtBaseDao.getGroupIdsForCluster(pinferId, clusterId);
        
        Map<Integer, WIdPickerProteinGroup> proteinGroups = new HashMap<Integer, WIdPickerProteinGroup>(protGroupIds.size()*2);
        
        // map of peptide groupID and peptide group
        Map<Integer, WIdPickerPeptideGroup> peptideGroups = new HashMap<Integer, WIdPickerPeptideGroup>();
        
        // get a list of protein groups
        for(int protGrpId: protGroupIds) {
            List<WIdPickerProtein> grpProteins = getGroupProteins(pinferId, protGrpId, peptideDef);
            WIdPickerProteinGroup grp = new WIdPickerProteinGroup(grpProteins);
            proteinGroups.put(protGrpId, grp);
            
            List<Integer> peptideGroupIds =  idpPeptBaseDao.getMatchingPeptGroupIds(pinferId, protGrpId);
            
            for(int peptGrpId: peptideGroupIds) {
                WIdPickerPeptideGroup peptGrp = peptideGroups.get(peptGrpId);
                if(peptGrp == null) {
                    List<IdPickerPeptideBase> groupPeptides = idpPeptBaseDao.loadIdPickerGroupPeptides(pinferId, peptGrpId);
                    peptGrp = new WIdPickerPeptideGroup(groupPeptides);
                    peptideGroups.put(peptGrpId, peptGrp);
                }
                peptGrp.addMatchingProteinGroupId(protGrpId);
            }
        }
        
        for(WIdPickerPeptideGroup peptGrp: peptideGroups.values()) {
            List<Integer> protGrpIds = peptGrp.getMatchingProteinGroupIds();
            if(protGrpIds.size() == 1) {
                proteinGroups.get(protGrpIds.get(0)).addUniqPeptideGrpId(peptGrp.getGroupId());
            }
            else {
                for(int protGrpId: protGrpIds)
                    proteinGroups.get(protGrpId).addNonUniqPeptideGrpId(peptGrp.getGroupId());
            }
        }
        
        WIdPickerCluster wCluster = new WIdPickerCluster(pinferId, clusterId);
        wCluster.setProteinGroups(new ArrayList<WIdPickerProteinGroup>(proteinGroups.values()));
        wCluster.setPeptideGroups(new ArrayList<WIdPickerPeptideGroup>(peptideGroups.values()));
        
        return wCluster;
    }
 
}
