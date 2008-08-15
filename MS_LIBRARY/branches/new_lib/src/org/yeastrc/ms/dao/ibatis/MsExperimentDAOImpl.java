/**
 * MsExperimentDAO.java
 * @author Vagisha Sharma
 * Jun 17, 2008
 * @version 1.0
 */
package org.yeastrc.ms.dao.ibatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yeastrc.ms.dao.MsExperimentDAO;
import org.yeastrc.ms.domain.MsExperiment;
import org.yeastrc.ms.domain.MsSearchDb;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 
 */
public class MsExperimentDAOImpl extends BaseSqlMapDAO implements MsExperimentDAO {
    
    public MsExperimentDAOImpl(SqlMapClient sqlMap) {
        super(sqlMap);
    }

    public MsSearchDb load(int msExperimentId) {
        return (MsSearchDb)queryForObject("MsExperiment.select", msExperimentId);
    }
    
    public int save(MsExperiment experiment) {
        return saveAndReturnId("MsExperiment.insert", experiment);
    }
    
    public List<Integer> selectAllExperimentIds() {
        return queryForList("MsExperiment.selectAll");
    }
    
    /**
     * Deletes the experiment
     */
    public void delete(int msExperimentId) {
        // delete the experiment
        delete("MsExperiment.delete", msExperimentId);
    }
}
