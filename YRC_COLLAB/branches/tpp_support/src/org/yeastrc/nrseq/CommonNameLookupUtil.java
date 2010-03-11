/**
 * CommonNameLookup.java
 * @author Vagisha Sharma
 * Apr 14, 2009
 * @version 1.0
 */
package org.yeastrc.nrseq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.bio.taxonomy.TaxonomyUtils;
import org.yeastrc.databases.flybase.FlyBaseUtils;
import org.yeastrc.databases.sangerpombe.PombeUtils;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.sgd.SGDUtils;
import org.yeastrc.wormbase.WormbaseUtils;

/**
 * 
 */
public class CommonNameLookupUtil {

    private static CommonNameLookupUtil instance;
    
    private static final Logger log = Logger.getLogger(CommonNameLookupUtil.class.getName());
    
    private CommonNameLookupUtil() {}
    
    public static CommonNameLookupUtil getInstance() {
        if(instance == null)
            instance = new CommonNameLookupUtil();
        return instance;
    }

    public List<ProteinCommonReference> getCommonReferences(List<String> accessionStrings, int speciesId) {
    	
    	if(accessionStrings == null)
    		return new ArrayList<ProteinCommonReference>(0);
    	
    	List<ProteinCommonReference> commonRefs = new ArrayList<ProteinCommonReference>();
    	
    	for(String accession: accessionStrings) {
    		ProteinCommonReference ref = getCommonReference(accession, speciesId);
    		if(ref != null) {
    			commonRefs.add(ref);
    		}
    	}
    	return commonRefs;
    }
    
    private ProteinCommonReference getCommonReference(String accession, int speciesId) {
		
    	String commonName = null;
    	String description = null;
    	StandardDatabase db = null;
    	if(speciesId == TaxonomyUtils.SACCHAROMYCES_CEREVISIAE) {
    		try {
				commonName = SGDUtils.getStandardName(accession);
				description = SGDUtils.getDescription(accession);
			} catch (SQLException e) {
				log.error("Error using SGDUtils", e);
			}
    		db = StandardDatabase.SGD;
    	}
    	else if(speciesId == TaxonomyUtils.SCHIZOSACCHAROMYCES_POMBE) {
    		try {
				commonName = PombeUtils.getStandardName(accession);
				description = PombeUtils.getDescription(accession);
			} catch (SQLException e) {
				log.error("Error using PombeUtils", e);
			}
    		db = StandardDatabase.S_POMBE;
    	}
    	else if (speciesId == TaxonomyUtils.CAENORHABDITIS_ELEGANS) {
    		try {
				commonName = WormbaseUtils.getStandardName(accession);
				description = WormbaseUtils.getDescription(accession);
			} catch (SQLException e) {
				log.error("Error using WormbaseUtils", e);
			}
    		db = StandardDatabase.WORMBASE;
    	}
    	else if (speciesId == TaxonomyUtils.DROSOPHILA_MELANOGASTER) {
    		try {
				commonName = FlyBaseUtils.getStandardName(accession);
				description = FlyBaseUtils.getDescription(accession);
			} catch (SQLException e) {
				log.error("Error using FlyBaseUtils", e);
			}
    		db = StandardDatabase.FLYBASE;
    	}
    	else if(speciesId == TaxonomyUtils.HOMO_SAPIENS) {
    		// TODO  For now there is not external database for human common name lookup
    		// the "HGNC (HUGO)" database in YRC_NRSEQ has common names in the
    		// accessionString column of tblProteinDatabase. That is what is being used. 
    	}
		if(commonName != null) {
			ProteinCommonReference ref = new ProteinCommonReference();
			ref.setAccession(accession);
			ref.setName(commonName);
			ref.setDescription(description);
			ref.setDatabase(db);
			return ref;
		}
		return null;
	}
    
//    public List<ProteinCommonReference> getCommonReferencesFromCache(int nrseqProteinId) throws Exception {
//        
//        Connection conn = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        
//        
//        try {
//            conn = DAOFactory.instance().getConnection();
//            String sql = "SELECT name, description FROM nrseqProteinCache WHERE proteinID="+nrseqProteinId+" ORDER BY name ";
//            stmt = conn.createStatement();
//            rs = stmt.executeQuery(sql);
//            
//            List<ProteinNameDescription> cndList = new ArrayList<ProteinNameDescription>();
//            
//            while(rs.next()) {
//                String name = rs.getString("name");
//                String description = rs.getString("description");
//                ProteinCommonReference cnd = new ProteinCommonReference();
//                cnd.setName(name);
//                cnd.setDescription(description);
//                cndList.add(cnd);
//            }
//            
//            listing.setNameAndDescription(cndList);
//        }
//        finally {
//            if(conn != null) try {conn.close(); conn = null;}
//            catch (SQLException e) {}
//            if(stmt != null) try {stmt.close(); stmt = null;}
//            catch (SQLException e) {}
//            if(rs != null) try {rs.close(); rs = null;}
//            catch (SQLException e) {}
//        }
//        
//        // If we did not find anything in the cache table look for it the old way
//        if(listing.getNameCount() == 0) {
//          NRProteinFactory nrpf = NRProteinFactory.getInstance();
//          NRProtein nrseqProt = null;
//          nrseqProt = (NRProtein)(nrpf.getProtein(nrseqProteinId));
//          
//          List<ProteinNameDescription> list = new ArrayList<ProteinNameDescription>();
//          String[] names = nrseqProt.getListing().split(",");
//          for(String name: names) {
//              ProteinNameDescription cnd = new ProteinNameDescription();
//              cnd.setName(name.trim());
//              cnd.setDescription(nrseqProt.getFullDescription());
//              list.add(cnd);
//          }
//          listing.setNameAndDescription(list);
//        }
//        
//        return listing;
//    }
    
//    public List<ProteinListing> getCommonListings(List<Integer> nrseqProteinIds) throws SQLException {
//        
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        
//        List<ProteinListing> listings = new ArrayList<ProteinListing>(nrseqProteinIds.size());
//        
//        try {
//            conn = DAOFactory.instance().getConnection();
//            String sql = "SELECT name, description FROM nrseqProteinCache WHERE proteinID=? ORDER BY name ";
//            stmt = conn.prepareStatement(sql);
//            
//            
//            for(int nrseqProteinId: nrseqProteinIds) {
//                stmt.setInt(1, nrseqProteinId);
//                rs = stmt.executeQuery();
//                ProteinListing listing = new ProteinListing();
//                listing.setNrseqProteinId(nrseqProteinId);
//                listings.add(listing);
//                
//                List<ProteinNameDescription> cndList = new ArrayList<ProteinNameDescription>();
//                
//                while(rs.next()) {
//                    String name = rs.getString("name");
//                    String description = rs.getString("description");
//                    ProteinNameDescription cnd = new ProteinNameDescription();
//                    cnd.setName(name);
//                    cnd.setDescription(description);
//                    cndList.add(cnd);
//                }
//                
//                listing.setNameAndDescription(cndList);
//                
//                rs.close();
//            }
//        }
//        finally {
//            if(conn != null) try {conn.close();}
//            catch (SQLException e) {}
//            if(stmt != null) try {stmt.close();}
//            catch (SQLException e) {}
//            if(rs != null) try {rs.close();}
//            catch (SQLException e) {}
//        }
//        
//        return listings;
//    }
    
    public List<Integer> getProteinIds(String commonName) throws SQLException {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        List<Integer> proteinIds = new ArrayList<Integer>();
        
        try {
            conn = DAOFactory.instance().getConnection();
            String sql = "SELECT proteinID FROM nrseqProteinCache WHERE name LIKE '"+commonName+"%'";
            stmt = conn.createStatement();
            
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                proteinIds.add(rs.getInt(1));
            }
        }
        finally {
            if(conn != null) try {conn.close();}
            catch (SQLException e) {}
            if(stmt != null) try {stmt.close();}
            catch (SQLException e) {}
            if(rs != null) try {rs.close();}
            catch (SQLException e) {}
        }
        
        return proteinIds;
    }
}
