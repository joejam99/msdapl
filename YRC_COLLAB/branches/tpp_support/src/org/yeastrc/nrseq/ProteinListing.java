/**
 * CommonName.java
 * @author Vagisha Sharma
 * Apr 14, 2009
 * @version 1.0
 */
package org.yeastrc.nrseq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.yeastrc.bio.taxonomy.TaxonomySearcher;
import org.yeastrc.ms.dao.nrseq.NrSeqLookupUtil;
import org.yeastrc.ms.domain.nrseq.NrProtein;



/**
 * 
 */
public class ProteinListing {

	private NrProtein protein;
    private List<ProteinCommonReference> commonReferences;
    private List<ProteinReference> references;
    
    
    public ProteinListing(NrProtein protein) {
    	
    	this.protein = protein;
    	commonReferences = new ArrayList<ProteinCommonReference>();
    	references = new ArrayList<ProteinReference>();
    }
    public void addReference(ProteinReference reference) {
    	references.add(reference);
    }
    
    public List<ProteinReference> getReferences() {
    	return this.references;
    }
    
    public void addCommonReference(ProteinCommonReference commonReference) {
    	commonReferences.add(commonReference);
    }
    
    public int getReferenceCount() {
        return this.references.size();
    }
    
    public int getCommonReferenceCount() {
    	return this.commonReferences.size();
    }
    
    public List<ProteinReference> getUniqueReferencesForNonStandardDatabases() throws SQLException {
    	
    	List<ProteinReference> refs = new ArrayList<ProteinReference>();
    	Set<String> seen = new HashSet<String>();
    	for(ProteinReference ref: this.references) {
    		if(StandardDatabase.isStandardDatabase(ref.getDatabaseName()))
    			continue;
    		if(seen.contains(ref.getAccession()))
    			continue;
    		seen.add(ref.getAccession());
    		refs.add(ref);
    	}
    	return refs;
    }
    
    public List<ProteinReference> getUniqueExternalReferences() throws SQLException {
    	
    	List<ProteinReference> refs = new ArrayList<ProteinReference>();
    	Set<Integer> seen = new HashSet<Integer>();
    	for(ProteinReference ref: this.references) {
    		if(ref.getUrl() == null) // no link to an external source
    			continue;
    		if(seen.contains(ref.getDatabaseId()))
    			continue;
    		seen.add(ref.getDatabaseId());
    		refs.add(ref);
    	}
    	return refs;
    }
    
    
    public List<ProteinReference> getReferencesForUniqueDescriptions() {
    	
    	Set<String> seen = new HashSet<String>();
    	List<ProteinReference> unique = new ArrayList<ProteinReference>();
    	for(ProteinReference ref: references) {
    		// ignore empty descriptions
    		if(ref.getDescription() == null || ref.getDescription().trim().length() == 0)
    			continue;
    		if(seen.contains(ref.getDescription()))
    			continue;
    		seen.add(ref.getDescription());
    		unique.add(ref);
    	}
    	return unique;
    }
    
    public ProteinReference getBestReference() throws SQLException {
    	
    	// First check if there is a standard database for this species
    	StandardDatabase standardDatabase = StandardDatabase.getStandardDatabaseForSpecies(protein.getSpeciesId());
    	if(standardDatabase != null) {
    		List<ProteinReference> references = getReferencesForDatabase(standardDatabase.getDatabaseName());
    		if(references.size() > 0)
    			return references.get(0); // return the first one
    	}
    	
    	// If we did not find reference to a standard database, return the first reference we have --
    	// this should be for the fasta file used for the search
    	String desc = references.get(0).getDescription();
    	if(desc != null && desc.trim().length() > 0)
    		return references.get(0);
    	
    	// If the fasta file used for the search did not have a non-empty description, return a description
    	// from one of the other standard database (NCBI-NR or Swiss-Prot).
    	List<ProteinReference> references = getUniqueReferencesForNonStandardDatabases();
    	for(ProteinReference ref: references) {
    		if(ref.getDescription() != null && ref.getDescription().trim().length() > 0)
    			return ref;
    	}
    	
    	// We did not find anything
    	return null;
    }
    
    public List<ProteinReference> getBestReferences() throws SQLException {
    	
    	// First check if there is a standard database for this species
    	StandardDatabase standardDatabase = StandardDatabase.getStandardDatabaseForSpecies(protein.getSpeciesId());
    	if(standardDatabase != null) {
    		List<ProteinReference> refs = getReferencesForDatabase(standardDatabase.getDatabaseName());
    		if(refs.size() > 0)
    			return refs;
    	}
    	
    	// If we did not find reference to a standard database for the species
    	// Look for a SwissProt reference
    	List<ProteinReference> refs = getReferencesForDatabase(standardDatabase.SWISSPROT.getDatabaseName());
		if(refs.size() > 0)
			return refs;
		
		// If we did not find a Swiss-Prot reference look for a NCBI-NR reference
		refs = getReferencesForDatabase(standardDatabase.NCBI_NR.getDatabaseName());
		if(refs.size() > 0)
			return refs;
    	
    	// We did not find anything
    	return null;
    }
    
    public List<String> getBestReferenceAccessions() throws SQLException {
    	
    	List<ProteinReference> refs = getBestReferences();
    	Set<String> accessions = new HashSet<String>();
    	for(ProteinReference ref: refs) {
    		accessions.add(ref.getAccession());
    	}
    	return new ArrayList<String>(accessions);
    }
    
    public List<ProteinReference> getReferencesForUniqueDatabases() {
    	
    	Set<Integer> seen = new HashSet<Integer>();
    	List<ProteinReference> unique = new ArrayList<ProteinReference>();
    	for(ProteinReference ref: references) {
    		if(seen.contains(ref.getDatabaseId()))
    			continue;
    		seen.add(ref.getDatabaseId());
    		unique.add(ref);
    	}
    	return unique;
    }
    
    
    List<ProteinReference> getReferencesForDatabase(String dbName) throws SQLException {
    	
    	List<ProteinReference> refs = new ArrayList<ProteinReference>();
    	for(ProteinReference ref: references) {
    		if(ref.getDatabaseName().equals(dbName))
    			refs.add(ref);
    	}
    	return refs;
    }
    
    List<String> getAccessionsForDatabase(String databaseName) throws SQLException {
    	
    	List<ProteinReference> refs = getReferencesForDatabase(databaseName);
    	Set<String> accessions = new HashSet<String>();
    	for(ProteinReference ref: refs) {
    		accessions.add(ref.getAccession());
    	}
    	return new ArrayList<String>(accessions);
    }
    
    List<String> getAccessionsForNotStandardDatabases() throws SQLException {
    	
    	List<ProteinReference> refs = getUniqueReferencesForNonStandardDatabases();
    	Set<String> accessions = new HashSet<String>();
    	for(ProteinReference ref: refs) {
    		accessions.add(ref.getAccession());
    	}
    	return new ArrayList<String>(accessions);
    }
    
    public List<ProteinCommonReference> getCommonReferences() {
    	return this.commonReferences;
    }
    
    public int getNrseqProteinId() {
        return protein.getId();
    }
    
    public String getSpeciesName() throws SQLException {
    	return TaxonomySearcher.getInstance().getName(getSpeciesId());
    }
    
    public int getSpeciesId() {
    	return protein.getSpeciesId();
    }
    
    public String getSequence() {
    	return NrSeqLookupUtil.getProteinSequence(protein.getId());
    }
}
