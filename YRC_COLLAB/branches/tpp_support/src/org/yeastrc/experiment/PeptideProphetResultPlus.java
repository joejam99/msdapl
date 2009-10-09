package org.yeastrc.experiment;

import java.math.BigDecimal;
import java.util.List;

import org.yeastrc.ms.domain.analysis.peptideProphet.PeptideProphetResult;
import org.yeastrc.ms.domain.run.MsScan;
import org.yeastrc.ms.domain.search.MsSearchResultPeptide;
import org.yeastrc.ms.domain.search.MsSearchResultProtein;
import org.yeastrc.ms.domain.search.ValidationStatus;

public class PeptideProphetResultPlus implements PeptideProphetResult {

    protected final PeptideProphetResult result;
    protected final int scanNumber;
    protected final BigDecimal retentionTime;
    private String filename;

    public PeptideProphetResultPlus(PeptideProphetResult result, MsScan scan) {
        super();
        this.result = result;
        this.scanNumber = scan.getStartScanNum();
        this.retentionTime = scan.getRetentionTime();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getScanNumber() {
        return scanNumber;
    }

    public BigDecimal getRetentionTime() {
        return retentionTime;
    }

    @Override
    public int getRunSearchAnalysisId() {
        return result.getRunSearchAnalysisId();
    }

    @Override
    public int getId() {
        return result.getId();
    }

    @Override
    public int getSearchResultId() {
        return result.getSearchResultId();
    }

    @Override
    public List<MsSearchResultProtein> getProteinMatchList() {
        return result.getProteinMatchList();
    }

    public String getProteins() {
        if(result.getProteinMatchList() == null)
            return null;
        else {
            StringBuilder buf = new StringBuilder();
            for(MsSearchResultProtein protein: result.getProteinMatchList()) {
                buf.append(", "+protein.getAccession());
            }
            if(buf.length() > 0)
                buf.deleteCharAt(0);
            if(buf.length() > 15) {
                buf.delete(15, buf.length());
                buf.append("...");
            }
            return buf.toString();
        }
    }

    @Override
    public int getRunSearchId() {
        return result.getRunSearchId();
    }

    @Override
    public int getScanId() {
        return result.getScanId();
    }

    @Override
    public int getCharge() {
        return result.getCharge();
    }

    @Override
    public BigDecimal getObservedMass() {
        return result.getObservedMass();
    }

    @Override
    public MsSearchResultPeptide getResultPeptide() {
        return result.getResultPeptide();
    }

    @Override
    public ValidationStatus getValidationStatus() {
        return result.getValidationStatus();
    }

    @Override
    public double getMassDifference() {
        return result.getMassDifference();
    }

    @Override
    public double getMassDifferenceRounded() {
        return result.getMassDifferenceRounded();
    }

    @Override
    public int getNumEnzymaticTermini() {
        return result.getNumEnzymaticTermini();
    }

    @Override
    public int getNumMissedCleavages() {
        return result.getNumMissedCleavages();
    }

    @Override
    public double getProbability() {
        return result.getProbability();
    }

    @Override
    public double getProbabilityNet_0() {
        return result.getProbabilityNet_0();
    }

    @Override
    public double getProbabilityNet_1() {
        return result.getProbabilityNet_1();
    }

    @Override
    public double getProbabilityNet_2() {
        return result.getProbabilityNet_2();
    }

    @Override
    public double getProbabilityRounded() {
        return result.getProbabilityRounded();
    }

    @Override
    public double getfVal() {
        return result.getfVal();
    }

    @Override
    public double getfValRounded() {
        return result.getfValRounded();
    }

    @Override
    public void setCharge(int charge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObservedMass(BigDecimal mass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResultPeptide(MsSearchResultPeptide resultPeptide) {
        throw new UnsupportedOperationException();
    }

}