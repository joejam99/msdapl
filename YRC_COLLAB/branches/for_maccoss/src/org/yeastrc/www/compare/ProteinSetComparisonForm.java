/**
 * ProteinferRunComparisionForm.java
 * @author Vagisha Sharma
 * Apr 10, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.bio.go.GOUtils;
import org.yeastrc.ms.domain.search.SORT_ORDER;

import edu.uwpr.protinfer.database.dto.ProteinFilterCriteria.SORT_BY;

/**
 * 
 */
public class ProteinSetComparisonForm extends ActionForm {

    private List<ProteinferRunFormBean> piRuns = new ArrayList<ProteinferRunFormBean>();
    private List<DTASelectRunFormBean> dtaRuns = new ArrayList<DTASelectRunFormBean>();

    private List<SelectableDataset> andList = new ArrayList<SelectableDataset>();
    private List<SelectableDataset> orList  = new ArrayList<SelectableDataset>();
    private List<SelectableDataset> notList = new ArrayList<SelectableDataset>();
    private List<SelectableDataset> xorList = new ArrayList<SelectableDataset>();
    
    private int pageNum = 1;
    
    private boolean download = false;
    private boolean collapseProteinGroups = false; // used only for downloading results
    private boolean includeDescription = false; // used only when downloading results
    private boolean goEnrichment = false;
    private boolean goEnrichmentGraph = false;
    
    private String nameSearchString;
    private String descriptionSearchString;
    
    private boolean onlyParsimonious = false;
    
    private boolean groupProteins = false;
    
    private SORT_BY sortBy = SORT_BY.NUM_PEPT;
    private SORT_ORDER sortOrder = SORT_ORDER.DESC;
    
    private String minMolWt;
    private String maxMolWt;
    private String minPi;
    private String maxPi;
    
    private int goAspect = GOUtils.BIOLOGICAL_PROCESS;
    private int speciesId;
    private String goEnrichmentPVal = "0.01";
    
    private boolean showFullDescriptions = false;
    private boolean keepProteinGroups = false;
    
    public boolean isShowFullDescriptions() {
        return showFullDescriptions;
    }

    public void setShowFullDescriptions(boolean showFullDescriptions) {
        this.showFullDescriptions = showFullDescriptions;
    }
    
    public boolean isKeepProteinGroups() {
        return keepProteinGroups;
    }

    public void setKeepProteinGroups(boolean keepProteinGroups) {
        this.keepProteinGroups = keepProteinGroups;
    }

    public boolean isGroupProteins() {
        return groupProteins;
    }

    public void setGroupProteins(boolean groupProteins) {
        this.groupProteins = groupProteins;
    }

    public boolean isOnlyParsimonious() {
        return onlyParsimonious;
    }

    public void setOnlyParsimonious(boolean onlyParsimonious) {
        this.onlyParsimonious = onlyParsimonious;
    }

    public String getNameSearchString() {
        return nameSearchString;
    }

    public void setNameSearchString(String nameSearchString) {
        this.nameSearchString = nameSearchString;
    }
    
    public String getDescriptionSearchString() {
        return descriptionSearchString;
    }

    public void setDescriptionSearchString(String descriptionSearchString) {
        this.descriptionSearchString = descriptionSearchString;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public List<ProteinferRunFormBean> getPiRuns() {
        return piRuns;
    }

    public void setPiRuns(List<ProteinferRunFormBean> piRuns) {
        this.piRuns = piRuns;
    }
    
    /**
     * Validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * an empty ActionErrors object.
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        // we need atleast two datasets runs to compare
        if (selectedRunCount() < 2) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.general.errorMessage", "Please select 2 or more experiments to compare."));
        }
        return errors;
    }

//    public void reset(ActionMapping mapping, HttpServletRequest request) {
//        // This needs to be set to false because if a checkbox is not checked the browser does not
//        // send its value in the request.
//        // http://struts.apache.org/1.1/faqs/ne...tml#checkboxes
//        this.keepProteinGroups = false;
//  }
    
    private int selectedRunCount() {
        int i = 0;
        for (ProteinferRunFormBean piRun: piRuns) {
            if (piRun != null && piRun.isSelected()) i++;
        }
        for(DTASelectRunFormBean dtaRun: dtaRuns) {
            if(dtaRun != null && dtaRun.isSelected()) i++;
        }
        return i;
    }
    
    //-----------------------------------------------------------------------------
    // Protein inference datasets
    //-----------------------------------------------------------------------------
    public ProteinferRunFormBean getProteinferRun(int index) {
        while(index >= piRuns.size())
            piRuns.add(new ProteinferRunFormBean());
        return piRuns.get(index);
    }
    
    public void setProteinferRunList(List <ProteinferRunFormBean> piRuns) {
        this.piRuns = piRuns;
    }
    
    public List <ProteinferRunFormBean> getProteinferRunList() {
        return piRuns;
    }
    
    public List<Integer> getSelectedProteinferRunIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for(ProteinferRunFormBean run: piRuns) {
            if(run != null && run.isSelected())
                ids.add(run.getRunId());
        }
        return ids;
    }
    
    
    //-----------------------------------------------------------------------------
    // DTASelect datasets
    //-----------------------------------------------------------------------------
    public DTASelectRunFormBean getDtaRun(int index) {
        while(index >= dtaRuns.size())
            dtaRuns.add(new DTASelectRunFormBean());
        return dtaRuns.get(index);
    }
    
    public void setDtaRunList(List <DTASelectRunFormBean> dtaRuns) {
        this.dtaRuns = dtaRuns;
    }
    
    public List <DTASelectRunFormBean> getDtaRunList() {
        return dtaRuns;
    }
    
    public List<Integer> getSelectedDtaRunIds() {
        List<Integer> ids = new ArrayList<Integer>();
        for(DTASelectRunFormBean run: dtaRuns) {
            if (run != null && run.isSelected())
                ids.add(run.getRunId());
        }
        return ids;
    }
    
    //-----------------------------------------------------------------------------
    // AND list
    //-----------------------------------------------------------------------------
    public SelectableDataset getAndDataset(int index) {
        while(index >= andList.size()) {
            andList.add(new SelectableDataset());
        }
        return andList.get(index);
    }
    
    public void setAndList(List<SelectableDataset> andList) {
        this.andList = andList;
    }
    
    public List<SelectableDataset> getAndList() {
        return andList;
    }
    
    //-----------------------------------------------------------------------------
    // OR list
    //-----------------------------------------------------------------------------
    public SelectableDataset getOrDataset(int index) {
        while(index >= orList.size()) {
            orList.add(new SelectableDataset());
        }
        return orList.get(index);
    }
    
    public void setOrList(List<SelectableDataset> orList) {
        this.orList = orList;
    }
    
    public List<SelectableDataset> getOrList() {
        return orList;
    }
    
    //-----------------------------------------------------------------------------
    // NOT list
    //-----------------------------------------------------------------------------
    public SelectableDataset getNotDataset(int index) {
        while(index >= notList.size()) {
            notList.add(new SelectableDataset());
        }
        return notList.get(index);
    }
    
    public void setNotList(List<SelectableDataset> notList) {
        this.notList = notList;
    }
    
    public List<SelectableDataset> getNotList() {
        return notList;
    }
    
    //-----------------------------------------------------------------------------
    // XOR list
    //-----------------------------------------------------------------------------
    public SelectableDataset getXorDataset(int index) {
        while(index >= xorList.size()) {
            xorList.add(new SelectableDataset());
        }
        return xorList.get(index);
    }
    
    public void setXorList(List<SelectableDataset> xorList) {
        this.xorList = xorList;
    }
    
    public List<SelectableDataset> getXorList() {
        return xorList;
    }

    //-----------------------------------------------------------------------------
    // Molecular Weight
    //-----------------------------------------------------------------------------
    public String getMinMolecularWt() {
        return minMolWt;
    }
    public Double getMinMolecularWtDouble() {
        if(minMolWt != null && minMolWt.trim().length() > 0)
            return Double.parseDouble(minMolWt);
        return null;
    }
    public void setMinMolecularWt(String molWt) {
        this.minMolWt = molWt;
    }
    
    public String getMaxMolecularWt() {
        return maxMolWt;
    }
    public Double getMaxMolecularWtDouble() {
        if(maxMolWt != null && maxMolWt.trim().length() > 0)
            return Double.parseDouble(maxMolWt);
        return null;
    }
    public void setMaxMolecularWt(String molWt) {
        this.maxMolWt = molWt;
    }
    
    //-----------------------------------------------------------------------------
    // pI
    //-----------------------------------------------------------------------------
    public String getMinPi() {
        return minPi;
    }
    public Double getMinPiDouble() {
        if(minPi != null && minPi.trim().length() > 0)
            return Double.parseDouble(minPi);
        return null;
    }
    public void setMinPi(String pi) {
        this.minPi = pi;
    }
    
    public String getMaxPi() {
        return maxPi;
    }
    public Double getMaxPiDouble() {
        if(maxPi != null && maxPi.trim().length() > 0)
            return Double.parseDouble(maxPi);
        return null;
    }
    public void setMaxPi(String pi) {
        this.maxPi = pi;
    }


    //-----------------------------------------------------------------------------
    // Sorting
    //-----------------------------------------------------------------------------
    public SORT_BY getSortBy() {
        return this.sortBy;
    }
    public String getSortByString() {
        if(sortBy == null)  return null;
        return this.sortBy.name();
    }
    
    public void setSortBy(SORT_BY sortBy) {
        this.sortBy = sortBy;
    }
    public void setSortByString(String sortBy) {
        this.sortBy = SORT_BY.getSortByForString(sortBy);
    }
    
    
    public SORT_ORDER getSortOrder() {
        return this.sortOrder;
    }
    public String getSortOrderString() {
        if(sortOrder == null)   return null;
        return this.sortOrder.name();
    }
    
    public void setSortOrder(SORT_ORDER sortOrder) {
        this.sortOrder = sortOrder;
    }
    public void setSortOrderString(String sortOrder) {
        this.sortOrder = SORT_ORDER.getSortByForName(sortOrder);
    }
    
    //-----------------------------------------------------------------------------
    // Download
    //-----------------------------------------------------------------------------
    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }
    
    public boolean isCollapseProteinGroups() {
        return this.collapseProteinGroups;
    }
    
    public void setCollapseProteinGroups(boolean collapse) {
        this.collapseProteinGroups = collapse;
    }
    
    public boolean isIncludeDescriptions() {
        return this.includeDescription;
    }
    
    public void setIncludeDescriptions(boolean include) {
        this.includeDescription = include;
    }

    //-----------------------------------------------------------------------------
    // GO Enrichment
    //-----------------------------------------------------------------------------
    public int getGoAspect() {
        return goAspect;
    }

    public void setGoAspect(int goAspect) {
        this.goAspect = goAspect;
    }

    public String getGoEnrichmentPVal() {
        return goEnrichmentPVal;
    }

    public void setGoEnrichmentPVal(String goEnrichmentPVal) {
        this.goEnrichmentPVal = goEnrichmentPVal;
    }

    public boolean isGoEnrichment() {
        return goEnrichment;
    }

    public void setGoEnrichment(boolean goEnrichment) {
        this.goEnrichment = goEnrichment;
    }
    
    public int getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public boolean isGoEnrichmentGraph() {
        return goEnrichmentGraph;
    }

    public void setGoEnrichmentGraph(boolean goEnrichmentGraph) {
        this.goEnrichmentGraph = goEnrichmentGraph;
    }
}
