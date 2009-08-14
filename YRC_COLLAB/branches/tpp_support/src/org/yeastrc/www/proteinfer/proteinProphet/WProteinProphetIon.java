/**
 * WProteinProphetIon.java
 * @author Vagisha Sharma
 * Aug 14, 2009
 * @version 1.0
 */
package org.yeastrc.www.proteinfer.proteinProphet;

import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.domain.protinfer.GenericProteinferIon;
import org.yeastrc.ms.domain.protinfer.ProteinferSpectrumMatch;
import org.yeastrc.ms.domain.search.MsSearchResult;

/**
 * 
 */
public class WProteinProphetIon {

    private GenericProteinferIon<? extends ProteinferSpectrumMatch> ion;
    private MsSearchResult bestSpectrumMatch;
    private boolean uniqueToProteinGrp = false;
    
    private List<Character> ntermResidues = new ArrayList<Character>();
    private List<Character> cTermResidues = new ArrayList<Character>();
    
    public WProteinProphetIon(GenericProteinferIon<? extends ProteinferSpectrumMatch> ion, MsSearchResult psm) {
        this.ion = ion;
        this.bestSpectrumMatch = psm;
    }

    public void addTerminalResidues(char nterm, char cterm) {
        this.ntermResidues.add(nterm);
        this.cTermResidues.add(cterm);
    }
    
    public int getScanId() {
        return bestSpectrumMatch.getScanId();
    }

    public GenericProteinferIon<? extends ProteinferSpectrumMatch> getIon() {
        return ion;
    }
    
    public MsSearchResult getBestSpectrumMatch() {
        return bestSpectrumMatch;
    }

    public boolean getIsUniqueToProteinGroup() {
        return uniqueToProteinGrp;
    }
    
    public void setIsUniqueToProteinGroup(boolean isUnique) {
        this.uniqueToProteinGrp = isUnique;
    }
    
    public String getIonSequence() {
        
        if(getBestSpectrumMatch() == null)
            return null;
        String seq = removeTerminalResidues(getBestSpectrumMatch().getResultPeptide().getModifiedPeptide());
        if(ntermResidues.size() == 0 & cTermResidues.size() == 0)
            return seq;
        
        seq = "."+seq+".";
        for(int i = 0; i < ntermResidues.size(); i++) {
            seq = "("+ntermResidues.get(i)+")"+seq+"("+cTermResidues.get(i)+")";
        }
        return seq;
    }

//    public String getIonSequence() {
//        return removeTerminalResidues(bestSpectrumMatch.getResultPeptide().getModifiedPeptide());
//    }
    
    public int getCharge() {
        return ion.getCharge();
    }
    
    public int getSpectrumCount() {
        return ion.getSpectrumCount();
    }
    
    protected static String removeTerminalResidues(String peptide) {
        int f = peptide.indexOf('.');
        int l = peptide.lastIndexOf('.');
        f = f == -1 ? 0 : f+1;
        l = l == -1 ? peptide.length() : l;
        return peptide.substring(f, l);
    }
}
