/**
 * TabularPercolatorResults.java
 * @author Vagisha Sharma
 * Apr 5, 2009
 * @version 1.0
 */
package org.yeastrc.experiment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.domain.search.SORT_BY;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.ms.service.ModifiedSequenceBuilderException;
import org.yeastrc.www.misc.Pageable;
import org.yeastrc.www.misc.TableCell;
import org.yeastrc.www.misc.TableHeader;
import org.yeastrc.www.misc.TableRow;
import org.yeastrc.www.misc.Tabular;
import org.yeastrc.www.util.RoundingUtils;

public class TabularPercolatorResults implements Tabular, Pageable {

	private List<SORT_BY> columns = new ArrayList<SORT_BY>();
    
    private SORT_BY sortColumn;
    private SORT_ORDER sortOrder = SORT_ORDER.ASC;
    
    private boolean hasPEP = true;
    
    private List<PercolatorResultPlus> results;
    private boolean hasBullsEyeArea = false;
    
    private int currentPage;
    private int lastPage = currentPage;
    private List<Integer> displayPageNumbers;
    
    private RoundingUtils rounder;
    
    public TabularPercolatorResults(List<PercolatorResultPlus> results, boolean hasPEP, boolean hasBullsEyeArea) {
        this.results = results;
        displayPageNumbers = new ArrayList<Integer>();
        displayPageNumbers.add(currentPage);
        
        this.hasPEP = hasPEP;
        this.hasBullsEyeArea = hasBullsEyeArea;
        
        columns.add(SORT_BY.FILE_ANALYSIS);
        columns.add(SORT_BY.SCAN);
        columns.add(SORT_BY.CHARGE); 
        columns.add(SORT_BY.MASS); 
        columns.add(SORT_BY.RT); 
        if(this.hasBullsEyeArea) {
        	columns.add(SORT_BY.AREA);
        }
        columns.add(SORT_BY.QVAL); 
        if(hasPEP)
        	columns.add(SORT_BY.PEP);
        else
        	columns.add(SORT_BY.DS); // Report Percolator Discriminant Score instead of PEP
        columns.add(SORT_BY.XCORR_RANK);
        columns.add(SORT_BY.XCORR);
        columns.add(SORT_BY.PEPTIDE);
        columns.add(SORT_BY.PROTEIN);
       
        this.rounder = RoundingUtils.getInstance();
    }
    
    @Override
    public int columnCount() {
        return columns.size();
    }

    public SORT_BY getSortedColumn() {
        return sortColumn;
    }
    
    public void setSortedColumn(SORT_BY column) {
        this.sortColumn = column;
    }
    
    public SORT_ORDER getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SORT_ORDER sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public List<TableHeader> tableHeaders() {
        List<TableHeader> headers = new ArrayList<TableHeader>(columnCount());
        for(SORT_BY col: columns) {
            TableHeader header = new TableHeader(col.getDisplayName(), col.name());
            
            if(col == SORT_BY.XCORR_RANK || col == SORT_BY.XCORR || col == SORT_BY.DELTACN)
                header.setSortable(false);
            if(col == SORT_BY.PROTEIN || col == SORT_BY.AREA)
                header.setSortable(false);
            
            if(col == sortColumn) {
                header.setSorted(true);
                header.setSortOrder(sortOrder);
            }
            headers.add(header);
        }
        return headers;
    }
    
    @Override
    public TableRow getRow(int index) {
        if(index >= results.size())
            return null;
        PercolatorResultPlus result = results.get(index);
        TableRow row = new TableRow();
        
        // row.addCell(new TableCell(String.valueOf(result.getPercolatorResultId())));
        TableCell cell = new TableCell(result.getFilename());
        row.addCell(cell);
        row.addCell(new TableCell(String.valueOf(result.getScanNumber())));
        row.addCell(new TableCell(String.valueOf(result.getCharge())));
        row.addCell(new TableCell(String.valueOf(rounder.roundFour(result.getObservedMass()))));
        
        // Retention time
        BigDecimal temp = result.getRetentionTime();
        if(temp == null) {
            row.addCell(new TableCell(""));
        }
        else
            row.addCell(new TableCell(String.valueOf(rounder.roundFour(temp))));
        
        // Area of the precursor ion
        if(hasBullsEyeArea) {
            row.addCell(new TableCell(String.valueOf(rounder.roundTwo(result.getArea()))));
        }
        
        row.addCell(new TableCell(String.valueOf(result.getQvalueRounded())));
        if(this.hasPEP)
            row.addCell(new TableCell(String.valueOf(result.getPosteriorErrorProbabilityRounded())));
        else
            row.addCell(new TableCell(String.valueOf(result.getDiscriminantScoreRounded())));
        
        // Sequest data
        row.addCell(new TableCell(String.valueOf(result.getSequestData().getxCorrRank())));
        row.addCell(new TableCell(String.valueOf(rounder.roundTwo(result.getSequestData().getxCorr()))));
//        row.addCell(new TableCell(String.valueOf(round(result.getSequestData().getDeltaCN()))));
        
        String url = "viewSpectrum.do?scanID="+result.getScanId()+"&runSearchResultID="+result.getId();
        try {
            cell = new TableCell(String.valueOf(result.getResultPeptide().getFullModifiedPeptide()), url, true);
        }
        catch (ModifiedSequenceBuilderException e) {
            cell = new TableCell("Error building peptide sequence");
        }
        row.addCell(cell);
        
        String cellContents = result.getOneProtein();
        if(result.getProteinCount() > 1) {
            cellContents += " <span class=\"underline clickable\" "+
            "onClick=javascript:toggleProteins("+result.getPercolatorResultId()+") "+
            ">("+result.getProteinCount()+")</span>";
            cellContents += " \n<div style=\"display: none;\" id=\"proteins_for_"
            			 +result.getPercolatorResultId()+"\">"+result.getOtherProteinsHtml()+"</div>";
        }
        cell = new TableCell(cellContents);
        cell.setClassName("left_align");
        row.addCell(cell);
        
        return row;
    }
    
    @Override
    public int rowCount() {
        return results.size();
    }

    @Override
    public void tabulate() {
        // nothing to do here?
    }

    
    
    @Override
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int pageNum) {
        this.currentPage = pageNum;
    }

    @Override
    public List<Integer> getDisplayPageNumbers() {
        return this.displayPageNumbers;
    }
    
    public void setDisplayPageNumbers(List<Integer> pageNums) {
        this.displayPageNumbers = pageNums;
    }

    @Override
    public int getLastPage() {
        return this.lastPage;
    }
    
    public void setLastPage(int pageNum) {
        this.lastPage = pageNum;
    }

    @Override
    public int getPageCount() {
        return lastPage;
    }

}