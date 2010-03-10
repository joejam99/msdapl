/**
 * ProteinDatasetSorter.java
 * @author Vagisha Sharma
 * Apr 18, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.yeastrc.ms.dao.nrseq.NrSeqLookupUtil;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.ms.util.ProteinUtils;
import org.yeastrc.www.compare.graph.ComparisonProteinGroup;

/**
 * 
 */
public class ComparisonProteinSorter {

    private static ComparisonProteinSorter instance;
    private ComparisonProteinSorter() {}
    
    public static ComparisonProteinSorter getInstance() {
        if(instance == null)
            instance = new ComparisonProteinSorter();
        return instance;
    }
    
    
    //-------------------------------------------------------------------------------------------------
    // SORT BY PEPTIDE COUNT
    //-------------------------------------------------------------------------------------------------
    public void sortByPeptideCount( List<ComparisonProtein> proteins, SORT_ORDER sortOrder) throws SQLException {
        for(ComparisonProtein protein: proteins) {
         // get the (max)number of peptides identified for this protein
            protein.setMaxPeptideCount(DatasetPeptideComparer.instance().getMaxPeptidesForProtein(protein));
        }
        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteins, new PeptideCountCompartorDesc());
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteins, new PeptideCountCompartorAsc());
    }
    
    public void sortGroupsByPeptideCount(List<ComparisonProteinGroup> proteinGroups, SORT_ORDER sortOrder) throws SQLException {

        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteinGroups, new Comparator<ComparisonProteinGroup>() {
                @Override
                public int compare(ComparisonProteinGroup o1,
                        ComparisonProteinGroup o2) {
                    return Integer.valueOf(o2.getMaxPeptideCount()).compareTo(o1.getMaxPeptideCount());
                }});
        
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteinGroups, new Comparator<ComparisonProteinGroup>() {
                @Override
                public int compare(ComparisonProteinGroup o1,
                        ComparisonProteinGroup o2) {
                    return Integer.valueOf(o1.getMaxPeptideCount()).compareTo(o2.getMaxPeptideCount());
                }});
    }
    
    //-------------------------------------------------------------------------------------------------
    // SORT BY MOLECULAR WT.
    //-------------------------------------------------------------------------------------------------
    public void sortByMolecularWeight(List<ComparisonProtein> proteins, SORT_ORDER sortOrder) {
        for(ComparisonProtein protein: proteins) {
            if(protein.molWtAndPiSet())
                continue;
            // get the protein properties
            String sequence = NrSeqLookupUtil.getProteinSequence(protein.getNrseqId());
            protein.setMolecularWeight( (float) (Math.round(ProteinUtils.calculateMolWt(sequence)*100) / 100.0));
            protein.setPi((float) (Math.round(ProteinUtils.calculatePi(sequence)*100) / 100.0));
        }
        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteins, new MolWtCompartorDesc());
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteins, new MolWtCompartorAsc());
    }
    
    public void sortGroupsByMolecularWeight(List<ComparisonProteinGroup> proteinGroups, SORT_ORDER sortOrder) {
        
        for(ComparisonProteinGroup proteinGroup: proteinGroups) {
            // get the protein properties
            sortByMolecularWeight(proteinGroup.getProteins(), sortOrder);
        }
        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteinGroups, new MolWtCompartorGroupDesc());
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteinGroups, new MolWtCompartorGroupAsc());
    }
    
    //-------------------------------------------------------------------------------------------------
    // SORT BY pI
    //-------------------------------------------------------------------------------------------------
    public void sortByPi(List<ComparisonProtein> proteins, SORT_ORDER sortOrder) {
        for(ComparisonProtein protein: proteins) {
            if(protein.molWtAndPiSet())
                continue;
            // get the protein properties
            String sequence = NrSeqLookupUtil.getProteinSequence(protein.getNrseqId());
            protein.setMolecularWeight( (float) (Math.round(ProteinUtils.calculateMolWt(sequence)*100) / 100.0));
            protein.setPi((float) (Math.round(ProteinUtils.calculatePi(sequence)*100) / 100.0));
        }
        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteins, new PiCompartorDesc());
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteins, new PiCompartorAsc());
    }
    
    public void sortGroupsByPi(List<ComparisonProteinGroup> proteinGroups, SORT_ORDER sortOrder) {
        
        for(ComparisonProteinGroup proteinGroup: proteinGroups) {
            // get the protein properties
            sortByPi(proteinGroup.getProteins(), sortOrder);
        }
        if(sortOrder == SORT_ORDER.DESC)
            Collections.sort(proteinGroups, new PiCompartorGroupDesc());
        if(sortOrder == SORT_ORDER.ASC)
            Collections.sort(proteinGroups, new PiCompartorGroupAsc());
    }
    
    
    
    private static class PeptideCountCompartorDesc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Integer.valueOf(o2.getMaxPeptideCount()).compareTo(o1.getMaxPeptideCount());
        }
    }
    
    private static class PeptideCountCompartorAsc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Integer.valueOf(o1.getMaxPeptideCount()).compareTo(o2.getMaxPeptideCount());
        }
    }
    
    private static class MolWtCompartorDesc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Float.valueOf(o2.getMolecularWeight()).compareTo(o1.getMolecularWeight());
        }
    }
    
    private static class MolWtCompartorAsc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Float.valueOf(o1.getMolecularWeight()).compareTo(o2.getMolecularWeight());
        }
    }
    
    private static class MolWtCompartorGroupDesc implements Comparator<ComparisonProteinGroup> {
        @Override
        public int compare(ComparisonProteinGroup o1, ComparisonProteinGroup o2) {
            return Float.valueOf(o2.getProteins().get(0).getMolecularWeight()).compareTo(o1.getProteins().get(0).getMolecularWeight());
        }
    }
    
    private static class MolWtCompartorGroupAsc implements Comparator<ComparisonProteinGroup> {
        @Override
        public int compare(ComparisonProteinGroup o1, ComparisonProteinGroup o2) {
            return Float.valueOf(o1.getProteins().get(0).getMolecularWeight()).compareTo(o2.getProteins().get(0).getMolecularWeight());
        }
    }
    
    private static class PiCompartorDesc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Float.valueOf(o2.getPi()).compareTo(o1.getPi());
        }
    }
    
    private static class PiCompartorAsc implements Comparator<ComparisonProtein> {
        @Override
        public int compare(ComparisonProtein o1, ComparisonProtein o2) {
            return Float.valueOf(o1.getPi()).compareTo(o2.getPi());
        }
    }
    
    private static class PiCompartorGroupDesc implements Comparator<ComparisonProteinGroup> {
        @Override
        public int compare(ComparisonProteinGroup o1, ComparisonProteinGroup o2) {
            return Float.valueOf(o2.getProteins().get(0).getPi()).compareTo(o1.getProteins().get(0).getPi());
        }
    }
    
    private static class PiCompartorGroupAsc implements Comparator<ComparisonProteinGroup> {
        @Override
        public int compare(ComparisonProteinGroup o1, ComparisonProteinGroup o2) {
            return Float.valueOf(o1.getProteins().get(0).getPi()).compareTo(o2.getProteins().get(0).getPi());
        }
    }
}