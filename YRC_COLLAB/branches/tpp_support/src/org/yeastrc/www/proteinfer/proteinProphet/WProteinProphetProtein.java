/**
 * WProteinProphetProtein.java
 * @author Vagisha Sharma
 * Aug 5, 2009
 * @version 1.0
 */
package org.yeastrc.www.proteinfer.proteinProphet;

import java.sql.SQLException;
import java.util.List;

import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProtein;
import org.yeastrc.ms.util.StringUtils;
import org.yeastrc.nrseq.ProteinListing;
import org.yeastrc.nrseq.ProteinReference;

/**
 * 
 */
public class WProteinProphetProtein {

    private ProteinProphetProtein prophetProtein;
    private ProteinListing listing;
    private float molecularWeight = -1.0f;
    private float pi = -1.0f;
    
    public WProteinProphetProtein(ProteinProphetProtein prot) {
        this.prophetProtein = prot;
    }
    public ProteinProphetProtein getProtein() {
        return prophetProtein;
    }
    
    public void setProteinListing(ProteinListing listing) {
    	this.listing = listing;
    }
    
    public ProteinListing getProteinListing() {
    	return this.listing;
    }
    
    public List<ProteinReference> getFastaReferences() throws SQLException {
    	return listing.getFastaReferences();
    }
    
    public String getAccessionsCommaSeparated() throws SQLException {
    	List<String> accessions = listing.getFastaReferenceAccessions();
    	return StringUtils.makeCommaSeparated(accessions);
    }
    
    public List<ProteinReference> getExternalReferences() throws SQLException {
    	return listing.getExternalReferences();
    }
    
    public List<ProteinReference> getAllReferences() throws SQLException {
    	return listing.getAllReferences();
    }
    
    public ProteinReference getOneBestReference() throws SQLException {
    	if(listing.getBestReferences().size() > 0)
    		return listing.getBestReferences().get(0);
    	return null;
    }
    
    public List<ProteinReference> getCommonReferences() {
    	return listing.getCommonReferences();
    }
    
    public String getCommonNamesCommaSeparated() throws SQLException {
    	List<String> names = listing.getCommonNames();
    	return StringUtils.makeCommaSeparated(names);
    }
    
    public void setMolecularWeight(float weight) {
        this.molecularWeight = weight;
    }
    
    public float getMolecularWeight() {
        return this.molecularWeight;
    }
    
    public float getPi() {
        return pi;
    }
    
    public void setPi(float pi) {
        this.pi = pi;
    }
}
